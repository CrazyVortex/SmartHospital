-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Anamakine: 127.0.0.1
-- Üretim Zamanı: 25 Kas 2026, 21:15:12
-- Sunucu sürümü: 10.4.32-MariaDB
-- PHP Sürümü: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Veritabanı: `smarthospital_db`
--

-- --------------------------------------------------------

--
-- Tablo için tablo yapısı `doktorlar`
--

CREATE TABLE `doktorlar` (
  `id` int(11) NOT NULL,
  `poliklinik_id` int(11) NOT NULL,
  `ad_soyad` varchar(120) NOT NULL,
  `unvan` varchar(60) DEFAULT 'Uzm. Dr.'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_turkish_ci;

--
-- Tablo döküm verisi `doktorlar`
--

INSERT INTO `doktorlar` (`id`, `poliklinik_id`, `ad_soyad`, `unvan`) VALUES
(1, 1, 'Ayşe Yılmaz', 'Uzm. Dr.'),
(2, 2, 'Mehmet Kaya', 'Uzm. Dr.');

-- --------------------------------------------------------

--
-- Tablo için tablo yapısı `hastalar`
--

CREATE TABLE `hastalar` (
  `id` int(11) NOT NULL,
  `tc_no` char(11) NOT NULL,
  `ad` varchar(60) NOT NULL,
  `soyad` varchar(60) NOT NULL,
  `dogum_tarihi` date NOT NULL,
  `cinsiyet` enum('K','E') NOT NULL,
  `telefon` varchar(20) NOT NULL,
  `kan_grubu` varchar(10) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_turkish_ci;

--
-- Tablo döküm verisi `hastalar`
--

INSERT INTO `hastalar` (`id`, `tc_no`, `ad`, `soyad`, `dogum_tarihi`, `cinsiyet`, `telefon`, `kan_grubu`) VALUES
(1, '11111111111', 'Ali', 'Demir', '1990-05-12', 'E', '5551112233', NULL),
(2, '22222222222', 'Zeynep', 'Çelik', '1985-03-20', 'K', '5554445566', NULL);

-- --------------------------------------------------------

--
-- Tablo için tablo yapısı `muayeneler`
--

CREATE TABLE `muayeneler` (
  `id` int(11) NOT NULL,
  `randevu_id` int(11) NOT NULL,
  `sikayet` text NOT NULL,
  `tani` text NOT NULL,
  `recete` text DEFAULT NULL,
  `olusturma_tarihi` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_turkish_ci;

-- --------------------------------------------------------

--
-- Tablo için tablo yapısı `poliklinikler`
--

CREATE TABLE `poliklinikler` (
  `id` int(11) NOT NULL,
  `ad` varchar(100) NOT NULL,
  `konum` varchar(100) DEFAULT 'Zemin Kat',
  `gunluk_kapasite` int(11) NOT NULL DEFAULT 20
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_turkish_ci;

--
-- Tablo döküm verisi `poliklinikler`
--

INSERT INTO `poliklinikler` (`id`, `ad`, `konum`, `gunluk_kapasite`) VALUES
(1, 'Dahiliye', 'Zemin Kat', 20),
(2, 'Kardiyoloji', 'Zemin Kat', 10);

-- --------------------------------------------------------

--
-- Tablo için tablo yapısı `randevular`
--

CREATE TABLE `randevular` (
  `id` int(11) NOT NULL,
  `hasta_id` int(11) NOT NULL,
  `doktor_id` int(11) NOT NULL,
  `randevu_tarihi` date NOT NULL,
  `randevu_saati` time NOT NULL,
  `durum` enum('bekliyor','muayenede','tamamlandi','iptal') NOT NULL DEFAULT 'bekliyor',
  `notlar` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_turkish_ci;

--
-- Tablo döküm verisi `randevular`
--

INSERT INTO `randevular` (`id`, `hasta_id`, `doktor_id`, `randevu_tarihi`, `randevu_saati`, `durum`, `notlar`) VALUES
(1, 1, 1, '2026-11-25', '09:00:00', 'bekliyor', NULL),
(2, 2, 1, '2026-11-25', '09:30:00', 'bekliyor', NULL),
(3, 1, 2, '2026-11-25', '10:00:00', 'bekliyor', NULL);

--
-- Dökümü yapılmış tablolar için indeksler
--

--
-- Tablo için indeksler `doktorlar`
--
ALTER TABLE `doktorlar`
  ADD PRIMARY KEY (`id`),
  ADD KEY `poliklinik_id` (`poliklinik_id`);

--
-- Tablo için indeksler `hastalar`
--
ALTER TABLE `hastalar`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `tc_no` (`tc_no`);

--
-- Tablo için indeksler `muayeneler`
--
ALTER TABLE `muayeneler`
  ADD PRIMARY KEY (`id`),
  ADD KEY `randevu_id` (`randevu_id`);

--
-- Tablo için indeksler `poliklinikler`
--
ALTER TABLE `poliklinikler`
  ADD PRIMARY KEY (`id`);

--
-- Tablo için indeksler `randevular`
--
ALTER TABLE `randevular`
  ADD PRIMARY KEY (`id`),
  ADD KEY `hasta_id` (`hasta_id`),
  ADD KEY `doktor_id` (`doktor_id`);

--
-- Dökümü yapılmış tablolar için AUTO_INCREMENT değeri
--

--
-- Tablo için AUTO_INCREMENT değeri `doktorlar`
--
ALTER TABLE `doktorlar`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- Tablo için AUTO_INCREMENT değeri `hastalar`
--
ALTER TABLE `hastalar`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- Tablo için AUTO_INCREMENT değeri `muayeneler`
--
ALTER TABLE `muayeneler`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- Tablo için AUTO_INCREMENT değeri `poliklinikler`
--
ALTER TABLE `poliklinikler`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- Tablo için AUTO_INCREMENT değeri `randevular`
--
ALTER TABLE `randevular`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- Dökümü yapılmış tablolar için kısıtlamalar
--

--
-- Tablo kısıtlamaları `doktorlar`
--
ALTER TABLE `doktorlar`
  ADD CONSTRAINT `doktorlar_ibfk_1` FOREIGN KEY (`poliklinik_id`) REFERENCES `poliklinikler` (`id`);

--
-- Tablo kısıtlamaları `muayeneler`
--
ALTER TABLE `muayeneler`
  ADD CONSTRAINT `muayeneler_ibfk_1` FOREIGN KEY (`randevu_id`) REFERENCES `randevular` (`id`) ON DELETE CASCADE;

--
-- Tablo kısıtlamaları `randevular`
--
ALTER TABLE `randevular`
  ADD CONSTRAINT `randevular_ibfk_1` FOREIGN KEY (`hasta_id`) REFERENCES `hastalar` (`id`),
  ADD CONSTRAINT `randevular_ibfk_2` FOREIGN KEY (`doktor_id`) REFERENCES `doktorlar` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
