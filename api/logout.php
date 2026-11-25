<?php
declare(strict_types=1);

require_once __DIR__ . '/auth.php';
start_secure_session();

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    header('Allow: POST');
    http_response_code(405);
    exit('Bu uç nokta yalnızca POST isteklerini kabul eder.');
}

destroy_secure_session();
header('Location: ../views/login.html', true, 303);
exit;
