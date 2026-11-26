<?php
// ÜYE 3 - 2. GÜN: Muayene kaydet (POST, JSON gövde)
// Gövde: {"randevu_id":1,"sikayet":"...","tani":"...","recete":"..."}
header('Content-Type: application/json; charset=utf-8');
require_once __DIR__ . '/../../config/database.php'; // $pdo

function cevap($kod, $ok, $mesaj, $data = null) {
    http_response_code($kod);
    echo json_encode(['ok' => $ok, 'message' => $mesaj, 'data' => $data], JSON_UNESCAPED_UNICODE);
    exit;
}

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    cevap(405, false, 'Sadece POST isteği kabul edilir.');
}

$girdi = json_decode(file_get_contents('php://input'), true);
if (!is_array($girdi)) {
    $girdi = $_POST; // normal form gönderimi için yedek
}

$randevuId = filter_var($girdi['randevu_id'] ?? null, FILTER_VALIDATE_INT);
$sikayet   = trim($girdi['sikayet'] ?? '');
$tani      = trim($girdi['tani'] ?? '');
$recete    = trim($girdi['recete'] ?? '');

if (!$randevuId || $randevuId <= 0) {
    cevap(400, false, 'Randevu seçilmelidir.');
}
if ($sikayet === '' || $tani === '') {
    cevap(400, false, 'Şikayet ve tanı alanları boş bırakılamaz.');
}

try {
    // Kilit randevu kaydı üzerinde alınır; aynı randevu için eşzamanlı
    // isteklerin birden fazla muayene oluşturması önlenir.
    $pdo->beginTransaction();

    $stmt = $pdo->prepare("SELECT durum FROM randevular WHERE id = ? FOR UPDATE");
    $stmt->execute([$randevuId]);
    $randevu = $stmt->fetch(PDO::FETCH_ASSOC);

    if (!$randevu) {
        $pdo->rollBack();
        cevap(404, false, 'Randevu bulunamadı.');
    }
    if ($randevu['durum'] === 'iptal' || $randevu['durum'] === 'tamamlandi') {
        $pdo->rollBack();
        cevap(409, false, 'Bu randevu için muayene kaydedilemez (durum: ' . $randevu['durum'] . ').');
    }

    // Muayene ve randevu durum güncellemesi birlikte tamamlanır.
    $stmt = $pdo->prepare("INSERT INTO muayeneler (randevu_id, sikayet, tani, recete) VALUES (?, ?, ?, ?)");
    $stmt->execute([$randevuId, $sikayet, $tani, $recete === '' ? null : $recete]);
    $yeniId = (int) $pdo->lastInsertId();

    $stmt = $pdo->prepare("UPDATE randevular SET durum = 'tamamlandi' WHERE id = ?");
    $stmt->execute([$randevuId]);

    $pdo->commit();
    cevap(200, true, 'Muayene kaydedildi, randevu tamamlandı.', ['muayene_id' => $yeniId]);
} catch (PDOException $e) {
    if ($pdo->inTransaction()) {
        $pdo->rollBack();
    }
    cevap(500, false, 'Veritabanı hatası.');
}
