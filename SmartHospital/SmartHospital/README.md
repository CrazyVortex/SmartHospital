# SmartHospital

Bu proje, üç geliştiricinin çalışmalarını mevcut işlevlerini koruyarak ortak bir standart altında birleştirmek için oluşturulmuştur. 

## Ortak Klasör Yapısı

```text
SmartHospital/
├── database/
│   └── schema.sql
├── backend/
│   ├── php/
│   │   ├── config/
│   │   │   └── database.php
│   │   └── api/
│   │       ├── auth/
│   │       ├── patients/
│   │       ├── appointments/
│   │       ├── examinations/
│   │       └── dashboard/
│   └── java/
│       └── src/
├── frontend/
│   ├── login/
│   ├── patients/
│   ├── appointments/
│   ├── examinations/
│   └── dashboard/
├── tests/
└── README.md

************************
# SmartHospital

Bu proje, üç geliştiricinin çalışmalarını mevcut işlevlerini koruyarak ortak bir standart altında birleştirmek için oluşturulmuştur.

## Ortak Klasör Yapısı

SmartHospital/
- database/
  - schema.sql
- backend/
  - php/
    - config/
      - database.php
    - api/
      - auth/
      - patients/
      - appointments/
      - examinations/
      - dashboard/
  - java/
    - src/
- frontend/
  - login/
  - patients/
  - appointments/
  - examinations/
  - dashboard/
- tests/
  - patients_appointments_check.php
- README.md

## Ekip Sorumlulukları

- Ana entegrasyon sorumlusu Azra'dır.
- Kullanıcı girişi, yetkilendirme ve Java modülünün sorumlusu Edanur'dur.
- Muayene ve dashboard modülünün sorumlusu Zeynep'tir.
- Hasta ve randevu modülü ile GitHub entegrasyonunun sorumlusu Azra'dır.

## Yerel Çalıştırma ve Test (Hasta ve Randevu Modülleri)

1. MySQL üzerinde `database/schema.sql` dosyasını çalıştırarak `smart_hospital` veritabanını ve tablolarını oluşturun.
2. `backend/php/config/database.php` dosyasındaki veritabanı bağlantı bilgilerini ortamınıza göre doğrulayın.
3. Randevu kayıtları oluşturmadan önce `clinics` ve `doctors` tablolarında en az birer kayıt bulunduğundan emin olun.
4. Bağlantı ve tablo kontrolü için `tests/patients_appointments_check.php` dosyasını çalıştırın.
5. Hasta ve randevu ekranlarını tarayıcı üzerinden açın:
   - Hasta Yönetimi: `frontend/patients/index.html`
   - Randevu Yönetimi: `frontend/appointments/index.html`