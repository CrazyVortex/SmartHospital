<?php
header('Content-Type: application/json; charset=utf-8');

require_once __DIR__ . '/../config/db.php';

try {
    $sql = "SELECT id, hasta_id, doktor_id, randevu_tarihi, randevu_saati, durum, notlar
            FROM randevular
            ORDER BY randevu_tarihi DESC, randevu_saati ASC";
    $stmt = $pdo->query($sql);
    $randevular = $stmt->fetchAll();

    echo json_encode([
        'ok' => true,
        'message' => 'Randevu listesi getirildi.',
        'data' => $randevular
    ], JSON_UNESCAPED_UNICODE);
} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode([
        'ok' => false,
        'message' => 'Randevular listelenirken bir hata oluştu (randevular tablosu henüz oluşturulmamış olabilir).',
        'data' => null
    ], JSON_UNESCAPED_UNICODE);
}