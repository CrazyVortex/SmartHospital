<?php
declare(strict_types=1);

if (PHP_SAPI !== 'cli') {
    http_response_code(404);
    exit;
}

require_once __DIR__ . '/database.php';

$input = json_decode(stream_get_contents(STDIN), true);
if (!is_array($input)) {
    fwrite(STDERR, "Girdi geçerli bir JSON nesnesi olmalıdır.\n");
    exit(1);
}

$username = $input['username'] ?? null;
$email = $input['email'] ?? null;
$password = $input['password'] ?? null;
$role = $input['role'] ?? null;
$allowedRoles = ['yonetici', 'doktor', 'personel'];

if (!is_string($username) || !is_string($email)
    || !is_string($password) || !is_string($role)) {
    fwrite(STDERR, "Kullanıcı adı, e-posta, şifre ve rol metin olmalıdır.\n");
    exit(1);
}

$username = trim($username);
$email = trim($email);
if ($username === '' || !filter_var($email, FILTER_VALIDATE_EMAIL)
    || $password === '' || !in_array($role, $allowedRoles, true)) {
    fwrite(STDERR, "Kullanıcı adı, geçerli e-posta, şifre ve geçerli rol zorunludur.\n");
    exit(1);
}

try {
    $statement = database_connection()->prepare(
        'INSERT INTO users (username, email, password_hash, role)
         VALUES (:username, :email, :password_hash, :role)'
    );
    $statement->execute([
        'username' => $username,
        'email' => $email,
        'password_hash' => password_hash($password, PASSWORD_DEFAULT),
        'role' => $role
    ]);
} catch (PDOException $exception) {
    error_log('SmartHospital user provisioning database error: ' . $exception->getMessage());
    fwrite(STDERR, "Kullanıcı oluşturulamadı. Veritabanı ayarlarını ve benzersiz alanları kontrol edin.\n");
    exit(1);
} catch (RuntimeException $exception) {
    error_log('SmartHospital user provisioning configuration error: ' . $exception->getMessage());
    fwrite(STDERR, "Kullanıcı oluşturulamadı. Veritabanı yapılandırması eksik.\n");
    exit(1);
}

fwrite(STDOUT, "Kullanıcı başarıyla oluşturuldu.\n");
