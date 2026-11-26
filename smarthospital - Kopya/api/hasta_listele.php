<?php
header('Content-Type: application/json; charset=utf-8');

require_once __DIR__ . '/../config/db.php';

try {
    $stmt = $pdo->query("SELECT id, tc_no, ad, soyad, dogum_tarihi, cinsiyet, telefon, kan_grubu FROM hastalar ORDER BY id DESC");
    $hastalar = $stmt->fetchAll();

    echo json_encode([
        'ok' => true,
        'message' => 'Hasta listesi getirildi.',
        'data' => $hastalar
    ], JSON_UNESCAPED_UNICODE);
} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode([
        'ok' => false,
        'message' => 'Hasta listelenirken bir hata oluştu.',
        'data' => null
    ], JSON_UNESCAPED_UNICODE);
}