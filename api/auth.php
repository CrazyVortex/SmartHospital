<?php
declare(strict_types=1);

function start_secure_session(): void
{
    if (session_status() === PHP_SESSION_ACTIVE) {
        return;
    }

    ini_set('session.use_strict_mode', '1');
    ini_set('session.use_only_cookies', '1');
    ini_set('session.cookie_httponly', '1');
    session_name('SMARTHOSPITALSESSID');

    $isHttps = isset($_SERVER['HTTPS'])
        && strtolower((string) $_SERVER['HTTPS']) !== 'off';

    session_set_cookie_params([
        'lifetime' => 0,
        'path' => '/',
        'secure' => $isHttps,
        'httponly' => true,
        'samesite' => 'Lax'
    ]);
    session_start();
}

function require_role(string $requiredRole): void
{
    start_secure_session();

    if (!isset($_SESSION['user_id'], $_SESSION['role'])) {
        header('Location: login.html?error=session', true, 303);
        exit;
    }

    $lastActivity = $_SESSION['last_activity'] ?? 0;
    if (!is_int($lastActivity) || time() - $lastActivity > 1800) {
        destroy_secure_session();
        header('Location: login.html?error=session', true, 303);
        exit;
    }

    if (!hash_equals($requiredRole, (string) $_SESSION['role'])) {
        http_response_code(403);
        header('Content-Type: text/html; charset=UTF-8');
        echo '<!doctype html><html lang="tr"><meta charset="utf-8"><title>Erişim reddedildi</title><body><h1>403 - Erişim reddedildi</h1><p>Bu sayfaya erişim yetkiniz yok.</p><a href="login.html">Giriş sayfasına dön</a></body></html>';
        exit;
    }

    $_SESSION['last_activity'] = time();
}

function destroy_secure_session(): void
{
    if (session_status() !== PHP_SESSION_ACTIVE) {
        return;
    }

    $_SESSION = [];
    $cookie = session_get_cookie_params();
    if (ini_get('session.use_cookies')) {
        setcookie(session_name(), '', [
            'expires' => time() - 42000,
            'path' => $cookie['path'],
            'domain' => $cookie['domain'],
            'secure' => $cookie['secure'],
            'httponly' => $cookie['httponly'],
            'samesite' => $cookie['samesite'] ?? 'Lax'
        ]);
    }
    session_destroy();
}
