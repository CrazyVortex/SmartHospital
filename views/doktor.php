<?php
declare(strict_types=1);

require_once __DIR__ . '/../api/auth.php';
require_role('doktor');
?>
<!doctype html>
<html lang="tr">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Doktor Paneli | SmartHospital</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="../assets/style.css" rel="stylesheet">
</head>
<body>
    <nav class="navbar app-navbar">
        <div class="container page-container">
            <a class="navbar-brand" href="doktor.php">SmartHospital</a>
            <form method="post" action="../api/logout.php">
                <button class="btn btn-outline-light btn-sm" type="submit">Çıkış yap</button>
            </form>
        </div>
    </nav>
    <main class="container page-container py-5">
        <header class="page-heading">
            <p class="text-uppercase small fw-semibold text-primary mb-2">Doktor paneli</p>
            <h1>Hoş geldiniz, <?= htmlspecialchars((string) $_SESSION['username'], ENT_QUOTES, 'UTF-8') ?></h1>
            <p class="mb-0">Bu alan yalnızca doktor rolündeki kullanıcılar içindir.</p>
        </header>
        <a class="btn btn-primary" href="randevular.html">Randevuları görüntüle</a>
    </main>
</body>
</html>
