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

$hasta_id = isset($_POST['hasta_id']) && $_POST['hasta_id'] !== '' ? (int) $_POST['hasta_id'] : 0;
$tc_no = trim($_POST['tc_no'] ?? '');
$ad = trim($_POST['ad'] ?? '');
$soyad = trim($_POST['soyad'] ?? '');
$dogum_tarihi = trim($_POST['dogum_tarihi'] ?? '');
$cinsiyet = strtoupper(trim($_POST['cinsiyet'] ?? ''));
$telefon = trim($_POST['telefon'] ?? '');
$kan_grubu = trim($_POST['kan_grubu'] ?? '');

if ($hasta_id < 0) {
    http_response_code(400);
    echo json_encode([
        'ok' => false,
        'message' => 'Geçersiz hasta ID değeri.',
        'data' => null
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

if ($tc_no === '' || $ad === '' || $soyad === '' || $dogum_tarihi === '' || $cinsiyet === '' || $telefon === '') {
    http_response_code(400);
    echo json_encode([
        'ok' => false,
        'message' => 'Lütfen zorunlu hasta alanlarını eksiksiz doldurun.',
        'data' => null
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

if (!preg_match('/^\d{11}$/', $tc_no)) {
    http_response_code(400);
    echo json_encode([
        'ok' => false,
        'message' => 'T.C. Kimlik No tam olarak 11 haneli rakamlardan oluşmalıdır.',
        'data' => null
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

if (!in_array($cinsiyet, ['K', 'E'], true)) {
    http_response_code(400);
    echo json_encode([
        'ok' => false,
        'message' => 'Cinsiyet değeri K veya E olmalıdır.',
        'data' => null
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

if (!preg_match('/^05\d{9}$/', $telefon)) {
    http_response_code(400);
    echo json_encode([
        'ok' => false,
        'message' => 'Telefon numarası başında 0 olacak şekilde 11 haneli (05XXXXXXXXX) olmalıdır.',
        'data' => null
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

$tarihGecerli = DateTime::createFromFormat('Y-m-d', $dogum_tarihi);
if (!$tarihGecerli || $tarihGecerli->format('Y-m-d') !== $dogum_tarihi) {
    http_response_code(400);
    echo json_encode([
        'ok' => false,
        'message' => 'Geçerli bir doğum tarihi giriniz.',
        'data' => null
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

try {
    if ($hasta_id > 0) {
        $varMiStmt = $pdo->prepare("SELECT id FROM hastalar WHERE id = :id LIMIT 1");
        $varMiStmt->execute([':id' => $hasta_id]);
        if (!$varMiStmt->fetch()) {
            http_response_code(404);
            echo json_encode([
                'ok' => false,
                'message' => 'Güncellenecek hasta kaydı bulunamadı.',
                'data' => null
            ], JSON_UNESCAPED_UNICODE);
            exit;
        }

        $cakismaStmt = $pdo->prepare("SELECT id FROM hastalar WHERE tc_no = :tc_no AND id != :id LIMIT 1");
        $cakismaStmt->execute([
            ':tc_no' => $tc_no,
            ':id' => $hasta_id
        ]);
        if ($cakismaStmt->fetch()) {
            http_response_code(409);
            echo json_encode([
                'ok' => false,
                'message' => 'Bu T.C. Kimlik No başka bir hastaya ait.',
                'data' => null
            ], JSON_UNESCAPED_UNICODE);
            exit;
        }

        $sql = "UPDATE hastalar
                SET tc_no = :tc_no,
                    ad = :ad,
                    soyad = :soyad,
                    dogum_tarihi = :dogum_tarihi,
                    cinsiyet = :cinsiyet,
                    telefon = :telefon,
                    kan_grubu = :kan_grubu
                WHERE id = :id";
        $stmt = $pdo->prepare($sql);
        $stmt->execute([
            ':tc_no' => $tc_no,
            ':ad' => $ad,
            ':soyad' => $soyad,
            ':dogum_tarihi' => $dogum_tarihi,
            ':cinsiyet' => $cinsiyet,
            ':telefon' => $telefon,
            ':kan_grubu' => ($kan_grubu !== '' ? $kan_grubu : null),
            ':id' => $hasta_id
        ]);

        echo json_encode([
            'ok' => true,
            'message' => 'Hasta bilgileri başarıyla güncellendi.',
            'data' => ['id' => $hasta_id]
        ], JSON_UNESCAPED_UNICODE);
        exit;
    }

    $kontrolStmt = $pdo->prepare("SELECT id FROM hastalar WHERE tc_no = :tc_no LIMIT 1");
    $kontrolStmt->execute([':tc_no' => $tc_no]);
    if ($kontrolStmt->fetch()) {
        http_response_code(409);
        echo json_encode([
            'ok' => false,
            'message' => 'Bu T.C. Kimlik No ile kayıtlı bir hasta zaten bulunuyor.',
            'data' => null
        ], JSON_UNESCAPED_UNICODE);
        exit;
    }

    $sql = "INSERT INTO hastalar (tc_no, ad, soyad, dogum_tarihi, cinsiyet, telefon, kan_grubu)
            VALUES (:tc_no, :ad, :soyad, :dogum_tarihi, :cinsiyet, :telefon, :kan_grubu)";
    $stmt = $pdo->prepare($sql);
    $stmt->execute([
        ':tc_no' => $tc_no,
        ':ad' => $ad,
        ':soyad' => $soyad,
        ':dogum_tarihi' => $dogum_tarihi,
        ':cinsiyet' => $cinsiyet,
        ':telefon' => $telefon,
        ':kan_grubu' => ($kan_grubu !== '' ? $kan_grubu : null)
    ]);

    $yeniId = (int) $pdo->lastInsertId();

    echo json_encode([
        'ok' => true,
        'message' => 'Hasta kaydı başarıyla eklendi.',
        'data' => ['id' => $yeniId]
    ], JSON_UNESCAPED_UNICODE);
} catch (PDOException $e) {
    if ((string) $e->getCode() === '23000') {
        http_response_code(409);
        echo json_encode([
            'ok' => false,
            'message' => 'Bu T.C. Kimlik No zaten kayıtlı.',
            'data' => null
        ], JSON_UNESCAPED_UNICODE);
        exit;
    }

    http_response_code(500);
    echo json_encode([
        'ok' => false,
        'message' => 'Hasta kaydedilirken veritabanı hatası oluştu.',
        'data' => null
    ], JSON_UNESCAPED_UNICODE);
}