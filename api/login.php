<?php
declare(strict_types=1);

require_once __DIR__ . '/auth.php';
require_once __DIR__ . '/database.php';

start_secure_session();

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    header('Allow: POST');
    http_response_code(405);
    exit('Bu uç nokta yalnızca POST isteklerini kabul eder.');
}

$identifier = trim((string) ($_POST['identifier'] ?? ''));
$password = (string) ($_POST['password'] ?? '');

if ($identifier === '' || $password === '') {
    header('Location: ../views/login.html?error=required', true, 303);
    exit;
}

try {
    $statement = database_connection()->prepare(
        'SELECT id, username, password_hash, role, is_active
         FROM users
         WHERE username = :username OR email = :email
         LIMIT 1'
    );
    $statement->execute([
        'username' => $identifier,
        'email' => $identifier
    ]);
    $user = $statement->fetch();
} catch (PDOException $exception) {
    error_log('SmartHospital login database error: ' . $exception->getMessage());
    header('Location: ../views/login.html?error=unavailable', true, 303);
    exit;
} catch (RuntimeException $exception) {
    error_log('SmartHospital login configuration error: ' . $exception->getMessage());
    header('Location: ../views/login.html?error=unavailable', true, 303);
    exit;
}

$allowedRoles = ['yonetici', 'doktor', 'personel'];
if ($user === false
    || (int) $user['is_active'] !== 1
    || !in_array($user['role'], $allowedRoles, true)
    || !password_verify($password, $user['password_hash'])) {
    header('Location: ../views/login.html?error=invalid', true, 303);
    exit;
}

session_regenerate_id(true);
$_SESSION = [
    'user_id' => (int) $user['id'],
    'username' => (string) $user['username'],
    'role' => (string) $user['role'],
    'last_activity' => time()
];

$roleDestinations = [
    'yonetici' => '../views/yonetim.html',
    'doktor' => '../views/doktor.php',
    'personel' => '../views/personel.php'
];

header('Location: ' . $roleDestinations[$user['role']], true, 303);
exit;
