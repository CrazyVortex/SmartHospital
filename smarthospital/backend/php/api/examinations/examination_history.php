<?php
// ÜYE 3 - 2. GÜN: Seçili randevunun hasta bilgisi + hastanın geçmiş muayeneleri
// Kullanım: api/examinations/examination_history.php?randevu_id=1
header('Content-Type: application/json; charset=utf-8');
require_once __DIR__ . '/../../config/database.php'; // $pdo

$randevuId = filter_input(INPUT_GET, 'randevu_id', FILTER_VALIDATE_INT);
if (!$randevuId || $randevuId <= 0) {
    http_response_code(400);
    echo json_encode(['ok' => false, 'message' => 'Geçerli bir randevu_id gerekli.', 'data' => null], JSON_UNESCAPED_UNICODE);
    exit;
}

try {
    // Randevu + hasta + doktor + poliklinik bilgisi
    $stmt = $pdo->prepare(
        "SELECT r.id, r.hasta_id, r.durum, CONCAT(h.ad, ' ', h.soyad) AS hasta_adi, h.tc_no,
                d.ad_soyad AS doktor, p.ad AS poliklinik
         FROM randevular r
         JOIN hastalar h ON h.id = r.hasta_id
         JOIN doktorlar d ON d.id = r.doktor_id
         JOIN poliklinikler p ON p.id = d.poliklinik_id
         WHERE r.id = ?"
    );
    $stmt->execute([$randevuId]);
    $randevu = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$randevu) {
        http_response_code(404);
        echo json_encode(['ok' => false, 'message' => 'Randevu bulunamadı.', 'data' => null], JSON_UNESCAPED_UNICODE);
        exit;
    }

    // Aynı hastanın önceki muayeneleri
    $stmt = $pdo->prepare(
        "SELECT m.id, m.sikayet, m.tani, m.recete, m.olusturma_tarihi, d.ad_soyad AS doktor
         FROM muayeneler m
         JOIN randevular r ON r.id = m.randevu_id
         JOIN doktorlar d ON d.id = r.doktor_id
         WHERE r.hasta_id = ?
         ORDER BY m.olusturma_tarihi DESC"
    );
    $stmt->execute([$randevu['hasta_id']]);
    $gecmis = $stmt->fetchAll(PDO::FETCH_ASSOC);

    echo json_encode([
        'ok' => true,
        'message' => 'Muayene geçmişi getirildi.',
        'data' => ['randevu' => $randevu, 'gecmis' => $gecmis]
    ], JSON_UNESCAPED_UNICODE);
} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode(['ok' => false, 'message' => 'Veritabanı hatası.', 'data' => null], JSON_UNESCAPED_UNICODE);
}
