<?php

declare(strict_types=1);

$_SERVER['REQUEST_METHOD'] = 'GET';
$_GET = [];

ob_start();
require_once __DIR__ . '/../backend/php/api/patients/index.php';
$rawOutput = ob_get_clean();

$decoded = json_decode($rawOutput ?: '', true);

$report = [
    'endpoint' => 'backend/php/api/patients/index.php',
    'is_valid_json' => json_last_error() === JSON_ERROR_NONE && is_array($decoded),
    'success_flag' => is_array($decoded) && isset($decoded['success']) ? (bool) $decoded['success'] : false,
    'record_count' => is_array($decoded) && isset($decoded['data']) && is_array($decoded['data']) ? count($decoded['data']) : 0,
    'raw_response' => $decoded ?? $rawOutput,
];

header('Content-Type: application/json; charset=utf-8');
echo json_encode($report, JSON_UNESCAPED_UNICODE | JSON_PRETTY_PRINT);