# Kimlik doğrulama kurulumu

## Gereksinimler

- PHP 7.3 veya üzeri ve `pdo_mysql` eklentisi
- Apache `mod_rewrite` ve `views/.htaccess` için `AllowOverride FileInfo`
- MySQL veya uyumlu MariaDB sunucusu

Projeyi PHP destekleyen web sunucusu üzerinden çalıştırın; HTML dosyalarını doğrudan `file://` ile açmak oturum veya API işlemlerini çalıştırmaz.

## Veritabanı

`database/schema.sql` dosyasını hedef veritabanında çalıştırın. Şema kullanıcı, poliklinik, doktor, hasta ve randevu tablolarını oluşturur. Aktif randevu saatlerinin tekilliği için veritabanı motoru `STORED` generated column/index desteklemelidir. PHP çalışma ortamında aşağıdaki ortam değişkenlerini tanımlayın:

- `DB_HOST`
- `DB_NAME`
- `DB_USER`
- `DB_PASS`

Bağlantı bilgilerini kaynak dosyalara veya sürüm kontrolüne eklemeyin.

PDO bağlantısı `config/database.php` içindedir. Mevcut PHP uç noktaları geriye uyumluluk için `api/database.php` üzerinden bu dosyayı yükler. PHP CLI ve `pdo_mysql` kurulu ortamda bağlantıyı şu komutla doğrulayın:

```powershell
php .\tests\database_connection_test.php
```

Kullanıcı hesapları ayrıca oluşturulmalıdır. `api/create_user.php`, yalnızca PHP CLI üzerinden JSON girdisi kabul eder ve parolayı `password_hash($password, PASSWORD_DEFAULT)` ile saklar; HTTP üzerinden çağrılamaz. Parolayı komut satırı argümanı veya shell geçmişinde görünecek bir değer olarak vermeyin. Girdiyi güvenli bir secret/pipe yöntemiyle standart girdiden iletin. Uygulamada açık web kaydı veya ilk yönetici oluşturma uç noktası bulunmaz.

## Yönetim sayfası koruması

Apache, `views/yonetim.html` isteklerini `views/yonetim.php` dosyasına yönlendiren rewrite kuralını çalıştırmalıdır. `mod_rewrite` etkin değilse veya `.htaccess` devre dışıysa statik HTML isteği PHP oturum kontrolünü atlayabilir; birleştirme/deploy öncesinde Apache yapılandırmasında kuralın etkin olduğunu doğrulayın.

Üretimde HTTPS kullanın. PHP, oturum çerezinin `Secure` niteliğini `HTTPS` sunucu değişkenine göre ayarlar.
