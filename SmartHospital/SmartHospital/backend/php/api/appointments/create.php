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

$patientId = isset($input['patient_id']) ? (int) $input['patient_id'] : 0;
$doctorId = isset($input['doctor_id']) ? (int) $input['doctor_id'] : 0;
$clinicId = isset($input['clinic_id']) ? (int) $input['clinic_id'] : 0;
$appointmentDate = isset($input['appointment_date']) ? trim((string) $input['appointment_date']) : '';
$appointmentTime = isset($input['appointment_time']) ? trim((string) $input['appointment_time']) : '';
$status = isset($input['status']) && in_array((string) $input['status'], ['scheduled', 'completed', 'cancelled'], true)
    ? (string) $input['status']
    : 'scheduled';
$notes = isset($input['notes']) && trim((string) $input['notes']) !== '' ? trim((string) $input['notes']) : null;

if ($patientId <= 0 || $doctorId <= 0 || $clinicId <= 0) {
    http_response_code(422);
    echo json_encode([
        'success' => false,
        'message' => 'Hasta, doktor ve poliklinik seçimi zorunludur.',
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

if ($appointmentDate === '' || $appointmentTime === '') {
    http_response_code(422);
    echo json_encode([
        'success' => false,
        'message' => 'Randevu tarihi ve saati zorunludur.',
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

try {
    $patientStmt = $pdo->prepare('SELECT id FROM patients WHERE id = :id LIMIT 1');
    $patientStmt->execute([':id' => $patientId]);
    if (!$patientStmt->fetch()) {
        http_response_code(404);
        echo json_encode([
            'success' => false,
            'message' => 'Seçilen hasta bulunamadı.',
        ], JSON_UNESCAPED_UNICODE);
        exit;
    }

    $doctorStmt = $pdo->prepare('SELECT id, clinic_id FROM doctors WHERE id = :id LIMIT 1');
    $doctorStmt->execute([':id' => $doctorId]);
    $doctor = $doctorStmt->fetch();
    if (!$doctor) {
        http_response_code(404);
        echo json_encode([
            'success' => false,
            'message' => 'Seçilen doktor bulunamadı.',
        ], JSON_UNESCAPED_UNICODE);
        exit;
    }

    if ((int) $doctor['clinic_id'] !== $clinicId) {
        http_response_code(422);
        echo json_encode([
            'success' => false,
            'message' => 'Seçilen doktor seçilen polikliniğe ait değil.',
        ], JSON_UNESCAPED_UNICODE);
        exit;
    }

    $conflictStmt = $pdo->prepare(
        'SELECT id FROM appointments
         WHERE doctor_id = :doctor_id
           AND appointment_date = :appointment_date
           AND appointment_time = :appointment_time
           AND status != :cancelled_status
         LIMIT 1'
    );
    $conflictStmt->execute([
        ':doctor_id' => $doctorId,
        ':appointment_date' => $appointmentDate,
        ':appointment_time' => $appointmentTime,
        ':cancelled_status' => 'cancelled',
    ]);
    if ($conflictStmt->fetch()) {
        http_response_code(409);
        echo json_encode([
            'success' => false,
            'message' => 'Bu doktor için seçilen tarih ve saatte aktif bir randevu zaten mevcut.',
        ], JSON_UNESCAPED_UNICODE);
        exit;
    }

    $sql = 'INSERT INTO appointments (patient_id, doctor_id, clinic_id, appointment_date, appointment_time, status, notes)
            VALUES (:patient_id, :doctor_id, :clinic_id, :appointment_date, :appointment_time, :status, :notes)';
    $stmt = $pdo->prepare($sql);
    $stmt->execute([
        ':patient_id' => $patientId,
        ':doctor_id' => $doctorId,
        ':clinic_id' => $clinicId,
        ':appointment_date' => $appointmentDate,
        ':appointment_time' => $appointmentTime,
        ':status' => $status,
        ':notes' => $notes,
    ]);

    $newId = (int) $pdo->lastInsertId();

    http_response_code(201);
    echo json_encode([
        'success' => true,
        'message' => 'Randevu başarıyla oluşturuldu.',
        'data' => [
            'id' => $newId,
            'patient_id' => $patientId,
            'doctor_id' => $doctorId,
            'clinic_id' => $clinicId,
            'appointment_date' => $appointmentDate,
            'appointment_time' => $appointmentTime,
            'status' => $status,
        ],
    ], JSON_UNESCAPED_UNICODE);
} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'message' => 'Randevu oluşturulurken veritabanı hatası oluştu.',
        'error' => $e->getMessage(),
    ], JSON_UNESCAPED_UNICODE);
}