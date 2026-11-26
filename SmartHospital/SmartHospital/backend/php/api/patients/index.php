<?php

declare(strict_types=1);

require_once __DIR__ . '/../../config/database.php';

if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    http_response_code(405);
    echo json_encode([
        'success' => false,
        'message' => 'Yalnızca GET isteklerine izin verilir.',
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

try {
    $search = isset($_GET['search']) ? trim((string) $_GET['search']) : '';
    $gender = isset($_GET['gender']) ? trim((string) $_GET['gender']) : '';

    $sql = 'SELECT id, tc_no, first_name, last_name, birth_date, gender, phone, email, address, created_at, updated_at
            FROM patients
            WHERE 1=1';
    $params = [];

    if ($search !== '') {
        $sql .= ' AND (tc_no LIKE :search_tc OR first_name LIKE :search_first OR last_name LIKE :search_last)';
        $like = '%' . $search . '%';
        $params[':search_tc'] = $like;
        $params[':search_first'] = $like;
        $params[':search_last'] = $like;
    }

    if ($gender !== '' && in_array($gender, ['female', 'male', 'other'], true)) {
        $sql .= ' AND gender = :gender';
        $params[':gender'] = $gender;
    }

    $sql .= ' ORDER BY id DESC';

    $stmt = $pdo->prepare($sql);
    $stmt->execute($params);
    $patients = $stmt->fetchAll();

    echo json_encode([
        'success' => true,
        'data' => $patients,
    ], JSON_UNESCAPED_UNICODE);
} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'message' => 'Hasta listesi alınırken veritabanı hatası oluştu.',
        'error' => $e->getMessage(),
    ], JSON_UNESCAPED_UNICODE);
}