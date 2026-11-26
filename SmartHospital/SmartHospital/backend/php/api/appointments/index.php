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
    $status = isset($_GET['status']) ? trim((string) $_GET['status']) : '';
    $appointmentDate = isset($_GET['appointment_date']) ? trim((string) $_GET['appointment_date']) : '';
    $clinicId = isset($_GET['clinic_id']) ? (int) $_GET['clinic_id'] : 0;
    $search = isset($_GET['search']) ? trim((string) $_GET['search']) : '';

    $sql = 'SELECT
                a.id,
                a.patient_id,
                a.doctor_id,
                a.clinic_id,
                a.appointment_date,
                a.appointment_time,
                a.status,
                a.notes,
                a.created_at,
                a.updated_at,
                p.tc_no AS patient_tc_no,
                p.first_name AS patient_first_name,
                p.last_name AS patient_last_name,
                d.full_name AS doctor_name,
                c.name AS clinic_name
            FROM appointments a
            INNER JOIN patients p ON a.patient_id = p.id
            INNER JOIN doctors d ON a.doctor_id = d.id
            INNER JOIN clinics c ON a.clinic_id = c.id
            WHERE 1=1';
    $params = [];

    if ($status !== '' && in_array($status, ['scheduled', 'completed', 'cancelled'], true)) {
        $sql .= ' AND a.status = :status';
        $params[':status'] = $status;
    }

    if ($appointmentDate !== '') {
        $sql .= ' AND a.appointment_date = :appointment_date';
        $params[':appointment_date'] = $appointmentDate;
    }

    if ($clinicId > 0) {
        $sql .= ' AND a.clinic_id = :clinic_id';
        $params[':clinic_id'] = $clinicId;
    }

    if ($search !== '') {
        $sql .= ' AND (p.tc_no LIKE :search_tc OR p.first_name LIKE :search_first OR p.last_name LIKE :search_last OR d.full_name LIKE :search_doctor OR c.name LIKE :search_clinic)';
        $like = '%' . $search . '%';
        $params[':search_tc'] = $like;
        $params[':search_first'] = $like;
        $params[':search_last'] = $like;
        $params[':search_doctor'] = $like;
        $params[':search_clinic'] = $like;
    }

    $sql .= ' ORDER BY a.appointment_date DESC, a.appointment_time DESC, a.id DESC';

    $stmt = $pdo->prepare($sql);
    $stmt->execute($params);
    $appointments = $stmt->fetchAll();

    echo json_encode([
        'success' => true,
        'data' => $appointments,
    ], JSON_UNESCAPED_UNICODE);
} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'message' => 'Randevu listesi alınırken veritabanı hatası oluştu.',
        'error' => $e->getMessage(),
    ], JSON_UNESCAPED_UNICODE);
}