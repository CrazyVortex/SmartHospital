<?php
header('Content-Type: application/json; charset=utf-8');
require_once __DIR__ . '/../../config/database.php';

$ortalamaMuayeneDk = 12;

try {
    $stmt = $pdo->prepare(
        "SELECT p.id, p.ad, p.gunluk_kapasite, COUNT(r.id) AS bekleyen
         FROM poliklinikler p
         LEFT JOIN doktorlar d ON d.poliklinik_id = p.id
         LEFT JOIN randevular r ON r.doktor_id = d.id
                                AND r.randevu_tarihi = CURDATE()
                                AND r.durum = 'bekliyor'
         GROUP BY p.id, p.ad, p.gunluk_kapasite"
    );
    $stmt->execute();
    $poliklinikler = $stmt->fetchAll(PDO::FETCH_ASSOC);

    $sonuc = [];
    foreach ($poliklinikler as $p) {
        $bekleyen = (int) $p['bekleyen'];
        $kapasite = (int) $p['gunluk_kapasite'];
        $oran = $kapasite > 0 ? round($bekleyen * 100 / $kapasite) : 0;

        if ($oran <= 50) {
            $seviye = 'normal';
        } elseif ($oran <= 80) {
            $seviye = 'yogun';
        } else {
            $seviye = 'kritik';
        }

        $sonuc[] = [
            'poliklinik' => $p['ad'],
            'bekleyen' => $bekleyen,
            'kapasite' => $kapasite,
            'oran' => $oran,
            'seviye' => $seviye,
            'tahmini_bekleme_dk' => $bekleyen * $ortalamaMuayeneDk
        ];
    }

    echo json_encode(['ok' => true, 'message' => 'Yoğunluk analizi hazır.', 'data' => $sonuc], JSON_UNESCAPED_UNICODE);
} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode(['ok' => false, 'message' => 'Veritabanı hatası.', 'data' => null], JSON_UNESCAPED_UNICODE);
}
