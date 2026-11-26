<?php

declare(strict_types=1);

require_once __DIR__ . '/../../config/database.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode([
        'success' => false,
        'message' => 'Yalnızca POST isteklerine izin verilir.',
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

$rawInput = file_get_contents('php://input');
$input = json_decode($rawInput ?: '', true);
if (!is_array($input) || $input === []) {
    $input = $_POST;
}

$tcNo = isset($input['tc_no']) ? trim((string) $input['tc_no']) : '';
$firstName = isset($input['first_name']) ? trim((string) $input['first_name']) : '';
$lastName = isset($input['last_name']) ? trim((string) $input['last_name']) : '';
$birthDate = isset($input['birth_date']) && trim((string) $input['birth_date']) !== '' ? trim((string) $input['birth_date']) : null;
$gender = isset($input['gender']) && in_array((string) $input['gender'], ['female', 'male', 'other'], true) ? (string) $input['gender'] : null;
$phone = isset($input['phone']) && trim((string) $input['phone']) !== '' ? trim((string) $input['phone']) : null;
$email = isset($input['email']) && trim((string) $input['email']) !== '' ? trim((string) $input['email']) : null;
$address = isset($input['address']) && trim((string) $input['address']) !== '' ? trim((string) $input['address']) : null;

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
    $checkStmt = $pdo->prepare('SELECT id FROM patients WHERE tc_no = :tc_no LIMIT 1');
    $checkStmt->execute([':tc_no' => $tcNo]);
    if ($checkStmt->fetch()) {
        http_response_code(409);
        echo json_encode([
            'success' => false,
            'message' => 'Bu T.C. kimlik numarasına ait kayıt zaten mevcut.',
        ], JSON_UNESCAPED_UNICODE);
        exit;
    }

    $sql = 'INSERT INTO patients (tc_no, first_name, last_name, birth_date, gender, phone, email, address)
            VALUES (:tc_no, :first_name, :last_name, :birth_date, :gender, :phone, :email, :address)';
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
    ]);

    $newId = (int) $pdo->lastInsertId();

    http_response_code(201);
    echo json_encode([
        'success' => true,
        'message' => 'Hasta kaydı başarıyla oluşturuldu.',
        'data' => [
            'id' => $newId,
            'tc_no' => $tcNo,
            'first_name' => $firstName,
            'last_name' => $lastName,
        ],
    ], JSON_UNESCAPED_UNICODE);
} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'message' => 'Hasta kaydı oluşturulurken veritabanı hatası oluştu.',
        'error' => $e->getMessage(),
    ], JSON_UNESCAPED_UNICODE);
}