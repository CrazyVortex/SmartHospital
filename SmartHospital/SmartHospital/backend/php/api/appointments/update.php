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

if ($id <= 0) {
    http_response_code(422);
    echo json_encode([
        'success' => false,
        'message' => 'Geçerli bir randevu id değeri gereklidir.',
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

try {
    $currentStmt = $pdo->prepare('SELECT * FROM appointments WHERE id = :id LIMIT 1');
    $currentStmt->execute([':id' => $id]);
    $current = $currentStmt->fetch();

    if (!$current) {
        http_response_code(404);
        echo json_encode([
            'success' => false,
            'message' => 'Güncellenecek randevu kaydı bulunamadı.',
        ], JSON_UNESCAPED_UNICODE);
        exit;
    }

    $doctorId = isset($input['doctor_id']) ? (int) $input['doctor_id'] : (int) $current['doctor_id'];
    $clinicId = isset($input['clinic_id']) ? (int) $input['clinic_id'] : (int) $current['clinic_id'];
    $appointmentDate = isset($input['appointment_date']) && trim((string) $input['appointment_date']) !== ''
        ? trim((string) $input['appointment_date'])
        : (string) $current['appointment_date'];
    $appointmentTime = isset($input['appointment_time']) && trim((string) $input['appointment_time']) !== ''
        ? trim((string) $input['appointment_time'])
        : (string) $current['appointment_time'];
    $status = isset($input['status']) && in_array((string) $input['status'], ['scheduled', 'completed', 'cancelled'], true)
        ? (string) $input['status']
        : (string) $current['status'];
    $notes = array_key_exists('notes', $input)
        ? (trim((string) $input['notes']) !== '' ? trim((string) $input['notes']) : null)
        : $current['notes'];

    if ($status !== 'cancelled') {
        $conflictStmt = $pdo->prepare(
            'SELECT id FROM appointments
             WHERE doctor_id = :doctor_id
               AND appointment_date = :appointment_date
               AND appointment_time = :appointment_time
               AND status != :cancelled_status
               AND id != :id
             LIMIT 1'
        );
        $conflictStmt->execute([
            ':doctor_id' => $doctorId,
            ':appointment_date' => $appointmentDate,
            ':appointment_time' => $appointmentTime,
            ':cancelled_status' => 'cancelled',
            ':id' => $id,
        ]);
        if ($conflictStmt->fetch()) {
            http_response_code(409);
            echo json_encode([
                'success' => false,
                'message' => 'Bu doktor için seçilen tarih ve saatte başka bir aktif randevu bulunuyor.',
            ], JSON_UNESCAPED_UNICODE);
            exit;
        }
    }

    $sql = 'UPDATE appointments
            SET doctor_id = :doctor_id,
                clinic_id = :clinic_id,
                appointment_date = :appointment_date,
                appointment_time = :appointment_time,
                status = :status,
                notes = :notes
            WHERE id = :id';
    $stmt = $pdo->prepare($sql);
    $stmt->execute([
        ':doctor_id' => $doctorId,
        ':clinic_id' => $clinicId,
        ':appointment_date' => $appointmentDate,
        ':appointment_time' => $appointmentTime,
        ':status' => $status,
        ':notes' => $notes,
        ':id' => $id,
    ]);

    echo json_encode([
        'success' => true,
        'message' => 'Randevu kaydı başarıyla güncellendi.',
    ], JSON_UNESCAPED_UNICODE);
} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'message' => 'Randevu güncellenirken veritabanı hatası oluştu.',
        'error' => $e->getMessage(),
    ], JSON_UNESCAPED_UNICODE);
}