<?php

declare(strict_types=1);

require_once __DIR__ . '/../../config/database.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST' && $_SERVER['REQUEST_METHOD'] !== 'PUT') {
    http_response_code(405);
    echo json_encode([
        'success' => false,
        'message' => 'Yalnızca POST veya PUT isteklerine izin verilir.',
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

$rawInput = file_get_contents('php://input');
$input = json_decode($rawInput ?: '', true);
if (!is_array($input) || $input === []) {
    $input = $_POST;
}

$id = isset($input['id']) ? (int) $input['id'] : 0;
$tcNo = isset($input['tc_no']) ? trim((string) $input['tc_no']) : '';
$firstName = isset($input['first_name']) ? trim((string) $input['first_name']) : '';
$lastName = isset($input['last_name']) ? trim((string) $input['last_name']) : '';
$birthDate = isset($input['birth_date']) && trim((string) $input['birth_date']) !== '' ? trim((string) $input['birth_date']) : null;
$gender = isset($input['gender']) && in_array((string) $input['gender'], ['female', 'male', 'other'], true) ? (string) $input['gender'] : null;
$phone = isset($input['phone']) && trim((string) $input['phone']) !== '' ? trim((string) $input['phone']) : null;
$email = isset($input['email']) && trim((string) $input['email']) !== '' ? trim((string) $input['email']) : null;
$address = isset($input['address']) && trim((string) $input['address']) !== '' ? trim((string) $input['address']) : null;

if ($id <= 0) {
    http_response_code(422);
    echo json_encode([
        'success' => false,
        'message' => 'Geçerli bir hasta id değeri gereklidir.',
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

if (!preg_match('/^[1-9][0-9]{10}$/', $tcNo)) {
    http_response_code(422);
    echo json_encode([
        'success' => false,
        'message' => 'Geçerli 11 haneli T.C. kimlik numarası giriniz.',
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

if ($firstName === '' || $lastName === '') {
    http_response_code(422);
    echo json_encode([
        'success' => false,
        'message' => 'Hasta adı ve soyadı zorunludur.',
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

try {
    $existsStmt = $pdo->prepare('SELECT id FROM patients WHERE id = :id LIMIT 1');
    $existsStmt->execute([':id' => $id]);
    if (!$existsStmt->fetch()) {
        http_response_code(404);
        echo json_encode([
            'success' => false,
            'message' => 'Güncellenecek hasta kaydı bulunamadı.',
        ], JSON_UNESCAPED_UNICODE);
        exit;
    }

    $dupStmt = $pdo->prepare('SELECT id FROM patients WHERE tc_no = :tc_no AND id != :id LIMIT 1');
    $dupStmt->execute([
        ':tc_no' => $tcNo,
        ':id' => $id,
    ]);
    if ($dupStmt->fetch()) {
        http_response_code(409);
        echo json_encode([
            'success' => false,
            'message' => 'Bu T.C. kimlik numarası başka bir hastaya ait.',
        ], JSON_UNESCAPED_UNICODE);
        exit;
    }

    $sql = 'UPDATE patients
            SET tc_no = :tc_no,
                first_name = :first_name,
                last_name = :last_name,
                birth_date = :birth_date,
                gender = :gender,
                phone = :phone,
                email = :email,
                address = :address
            WHERE id = :id';
    $stmt = $pdo->prepare($sql);
    $stmt->execute([
        ':tc_no' => $tcNo,
        ':first_name' => $firstName,
        ':last_name' => $lastName,
        ':birth_date' => $birthDate,
        ':gender' => $gender,
        ':phone' => $phone,
        ':email' => $email,
        ':address' => $address,
        ':id' => $id,
    ]);

    echo json_encode([
        'success' => true,
        'message' => 'Hasta bilgileri başarıyla güncellendi.',
    ], JSON_UNESCAPED_UNICODE);
} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'message' => 'Hasta güncellenirken veritabanı hatası oluştu.',
        'error' => $e->getMessage(),
    ], JSON_UNESCAPED_UNICODE);
}