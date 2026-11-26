<?php
// Hata raporlamayı geliştirme aşamasında açık tut, JSON çıktılarını bozmaması için ekrana basma
error_reporting(E_ALL);
ini_set('display_errors', 0);

if (session_status() === PHP_SESSION_NONE) {
    session_start();
}

define('DB_HOST', 'localhost');
define('DB_NAME', 'smarthospital_db');
define('DB_USER', 'root');
define('DB_PASS', ''); // XAMPP varsayılan şifresi boştur
define('DB_CHARSET', 'utf8mb4');

// 1. PDO Bağlantısı (Modern ve güvenli sorgular için)
$dsn = "mysql:host=" . DB_HOST . ";dbname=" . DB_NAME . ";charset=" . DB_CHARSET;
$options = [
    PDO::ATTR_ERRMODE            => PDO::ERRMODE_EXCEPTION,
    PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC,
    PDO::ATTR_EMULATE_PREPARES   => false,
];

try {
    $pdo = new PDO($dsn, DB_USER, DB_PASS, $options);
} catch (PDOException $e) {
    header('Content-Type: application/json; charset=utf-8');
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'message' => 'Veritabanı bağlantı hatası (PDO): ' . $e->getMessage()
    ], JSON_UNESCAPED_UNICODE);
    exit;
}

// 2. MySQLi Bağlantısı (Diğer modüllerdeki klasik mysqli sorgularıyla tam uyumluluk için)
$conn = new mysqli(DB_HOST, DB_USER, DB_PASS, DB_NAME);
if ($conn->connect_error) {
    header('Content-Type: application/json; charset=utf-8');
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'message' => 'Veritabanı bağlantı hatası (MySQLi): ' . $conn->connect_error
    ], JSON_UNESCAPED_UNICODE);
    exit;
}
$conn->set_charset(DB_CHARSET);

// Ortak JSON yanıt fonksiyonu
if (!function_exists('jsonResponse')) {
    function jsonResponse(array $data, int $statusCode = 200): void {
        header('Content-Type: application/json; charset=utf-8');
        http_response_code($statusCode);
        echo json_encode($data, JSON_UNESCAPED_UNICODE);
        exit;
    }
}
?>