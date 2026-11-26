<?php
header('Content-Type: application/json; charset=utf-8');

require_once __DIR__ . '/../config/db.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode([
        'ok' => false,
        'message' => 'Yalnızca POST isteği kabul edilir.',
        'data' => null
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

$randevu_id = isset($_POST['randevu_id']) ? (int) $_POST['randevu_id'] : 0;
$yeniDurum = strtolower(trim($_POST['durum'] ?? ''));
$not = trim($_POST['notlar'] ?? '');
$gecerliDurumlar = ['bekliyor', 'muayenede', 'tamamlandi', 'iptal'];

if ($randevu_id <= 0) {
    http_response_code(400);
    echo json_encode([
        'ok' => false,
        'message' => 'Geçerli bir randevu ID seçilmelidir.',
        'data' => null
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

if (!in_array($yeniDurum, $gecerliDurumlar, true)) {
    http_response_code(400);
    echo json_encode([
        'ok' => false,
        'message' => 'Geçersiz randevu durumu. İzin verilenler: bekliyor, muayenede, tamamlandi, iptal.',
        'data' => null
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

try {
    $kontrolStmt = $pdo->prepare("SELECT id, durum FROM randevular WHERE id = :id LIMIT 1");
    $kontrolStmt->execute([':id' => $randevu_id]);
    $mevcut = $kontrolStmt->fetch();

    if (!$mevcut) {
        http_response_code(404);
        echo json_encode([
            'ok' => false,
            'message' => 'Randevu kaydı bulunamadı.',
            'data' => null
        ], JSON_UNESCAPED_UNICODE);
        exit;
    }

    $mevcutDurum = strtolower((string) ($mevcut['durum'] ?? 'bekliyor'));

    if ($mevcutDurum === 'tamamlandi' && $yeniDurum !== 'tamamlandi') {
        http_response_code(409);
        echo json_encode([
            'ok' => false,
            'message' => 'Tamamlanmış bir randevunun durumu geri alınamaz veya değiştirilemez.',
            'data' => null
        ], JSON_UNESCAPED_UNICODE);
        exit;
    }

    if ($not !== '') {
        $guncelleStmt = $pdo->prepare("UPDATE randevular SET durum = :durum, notlar = :notlar WHERE id = :id");
        $guncelleStmt->execute([
            ':durum' => $yeniDurum,
            ':notlar' => $not,
            ':id' => $randevu_id
        ]);
    } else {
        $guncelleStmt = $pdo->prepare("UPDATE randevular SET durum = :durum WHERE id = :id");
        $guncelleStmt->execute([
            ':durum' => $yeniDurum,
            ':id' => $randevu_id
        ]);
    }

    echo json_encode([
        'ok' => true,
        'message' => 'Randevu durumu güncellendi.',
        'data' => [
            'id' => $randevu_id,
            'onceki_durum' => $mevcutDurum,
            'durum' => $yeniDurum
        ]
    ], JSON_UNESCAPED_UNICODE);
} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode([
        'ok' => false,
        'message' => 'Randevu durumu güncellenirken veritabanı hatası oluştu.',
        'data' => null
    ], JSON_UNESCAPED_UNICODE);
}
