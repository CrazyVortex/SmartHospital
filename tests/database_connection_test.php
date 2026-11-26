<?php
declare(strict_types=1);

if (PHP_SAPI !== 'cli') {
    http_response_code(404);
    exit;
}

require_once __DIR__ . '/../config/database.php';

try {
    $result = database_connection()->query('SELECT 1')->fetchColumn();
    if ((int) $result !== 1) {
        throw new RuntimeException('Database connectivity check returned an unexpected result.');
    }

    fwrite(STDOUT, "Database connection successful.\n");
} catch (PDOException $exception) {
    error_log('SmartHospital database connectivity test failed: ' . $exception->getMessage());
    fwrite(STDERR, "Database connection failed. Check DB_* settings and MySQL availability.\n");
    exit(1);
} catch (RuntimeException $exception) {
    fwrite(STDERR, 'Database connection test failed: ' . $exception->getMessage() . "\n");
    exit(1);
}
