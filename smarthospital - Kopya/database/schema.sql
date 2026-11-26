CREATE DATABASE IF NOT EXISTS smarthospital_db CHARACTER SET utf8mb4 COLLATE utf8mb4_turkish_ci;
USE smarthospital_db;

CREATE TABLE IF NOT EXISTS hastalar (
    id INT AUTO_INCREMENT PRIMARY KEY,
    tc_no CHAR(11) NOT NULL UNIQUE,
    ad VARCHAR(60) NOT NULL,
    soyad VARCHAR(60) NOT NULL,
    dogum_tarihi DATE NOT NULL,
    cinsiyet ENUM('K', 'E') NOT NULL,
    telefon VARCHAR(20) NOT NULL,
    kan_grubu VARCHAR(10) NULL
);
```[cite: 7, 9]

*(Eğer masaüstünde veya başka bir yerde oluşturduğun eski `şema.sql` dosyası kaldıysa onu silebilirsin ki kafa karıştırmasın.)*

Kaydettikten sonra **phpMyAdmin -> SQL** sekmesinden bu kodları çalıştırıp veritabanı ile `hastalar` tablosunu oluşturabilirsin[cite: 7, 9]. Hazır olduğunda haber ver, **`config/db.php`** ve **`api/hasta_listele.php`** adımlarına geçelim[cite: 5, 9]. 