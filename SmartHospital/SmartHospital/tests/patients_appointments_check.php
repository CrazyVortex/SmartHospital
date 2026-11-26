<?php

declare(strict_types=1);

require_once __DIR__ . '/../backend/php/config/database.php';

try {
    $tablesToCheck = ['patients', 'appointments', 'clinics', 'doctors'];
    $results = [];

    foreach ($tablesToCheck as $table) {
        $stmt = $pdo->query("SHOW TABLES LIKE '{$table}'");
        $exists = (bool) $stmt->fetchColumn();

        $count = null;
        if ($exists) {
            $countStmt = $pdo->query("SELECT COUNT(*) FROM `{$table}`");
            $count = (int) $countStmt->fetchColumn();
        }

        $results[$table] = [
            'exists' => $exists,
            'row_count' => $count,
        ];
    }

    echo json_encode([
        'success' => true,
        'message' => 'Veritabanı bağlantısı ve tablo kontrolü tamamlandı.',
        'tables' => $results,
    ], JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT);
} catch (Throwable $e) {
    http_response_code(500);
    echo json_encode([
        'success' => false,
        'message' => 'Kontrol sırasında hata oluştu.',
        'error' => $e->getMessage(),
    ], JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT);
}