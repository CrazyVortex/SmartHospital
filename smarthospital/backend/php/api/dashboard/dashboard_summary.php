<?php
// ÜYE 3 - Dashboard özet sayaçları + bugünün randevuları (2. Gün'de liste eklendi)
header('Content-Type: application/json; charset=utf-8');
require_once __DIR__ . '/../../config/database.php'; // $pdo

try {
    $stmt = $pdo->prepare("SELECT COUNT(*) FROM randevular WHERE randevu_tarihi = CURDATE()");
    $stmt->execute();
    $bugunToplam = (int) $stmt->fetchColumn();

    $stmt = $pdo->prepare("SELECT COUNT(*) FROM hastalar");
    $stmt->execute();
    $kayitliHasta = (int) $stmt->fetchColumn();

    $stmt = $pdo->prepare("SELECT COUNT(*) FROM randevular WHERE randevu_tarihi = CURDATE() AND durum = 'bekliyor'");
    $stmt->execute();
    $bekleyen = (int) $stmt->fetchColumn();

    $stmt = $pdo->prepare("SELECT COUNT(*) FROM randevular WHERE randevu_tarihi = CURDATE() AND durum = 'tamamlandi'");
    $stmt->execute();
    $tamamlanan = (int) $stmt->fetchColumn();

    // Bugünün randevuları (4 tabloyu JOIN ile birleştirir)
    $stmt = $pdo->prepare(
        "SELECT r.id, r.randevu_saati, CONCAT(h.ad, ' ', h.soyad) AS hasta_adi, h.tc_no,
                p.ad AS poliklinik, d.ad_soyad AS doktor, r.durum
         FROM randevular r
         JOIN hastalar h ON h.id = r.hasta_id
         JOIN doktorlar d ON d.id = r.doktor_id
         JOIN poliklinikler p ON p.id = d.poliklinik_id
         WHERE r.randevu_tarihi = CURDATE()
         ORDER BY r.randevu_saati"
    );
    $stmt->execute();
    $randevular = $stmt->fetchAll(PDO::FETCH_ASSOC);

    echo json_encode([
        'ok' => true,
        'message' => 'Dashboard özeti getirildi.',
        'data' => [
            'bugun_toplam_randevu' => $bugunToplam,
            'kayitli_hasta'        => $kayitliHasta,
            'bekleyen_hasta'       => $bekleyen,
            'tamamlanan_muayene'   => $tamamlanan,
            'bugunun_randevulari'  => $randevular
        ]
    ], JSON_UNESCAPED_UNICODE);
} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode(['ok' => false, 'message' => 'Veritabanı hatası.', 'data' => null], JSON_UNESCAPED_UNICODE);
}
