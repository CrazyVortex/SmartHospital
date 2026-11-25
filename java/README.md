# SmartHospital Java API

This Java 17 API uses the JDK `com.sun.net.httpserver.HttpServer`; it has no
third-party runtime dependencies. The first implementation keeps appointments
in the existing sample `RandevuKontrolServisi` in memory. Data is reset when
the process restarts. PHP and MySQL are not connected.

## Run

From the project root in PowerShell, use the standalone launcher. It checks
whether the port is occupied, compiles the Java sources, starts the server,
and keeps it in the foreground. Press `Ctrl+C` in that console to run the
Java shutdown hook and stop it gracefully:

```powershell
.\java\run-server.ps1 -Port 8081 -AllowedOrigins "http://localhost:5500"
```

The server binds only to `127.0.0.1`. `SMART_HOSPITAL_PORT` is optional and
defaults to `8081`. `SMART_HOSPITAL_ALLOWED_ORIGINS` is a comma-separated
allow-list of exact HTTP(S) origins; it defaults to empty, which disables
cross-origin browser access. Do not treat CORS as authentication.

## Endpoints

All responses use JSON. Successful responses use `{"data": ...}` and errors
use `{"error":{"code":"...","message":"..."}}`.

| Method | Path | Purpose |
|---|---|---|
| GET | `/api/health` | Check API availability; reports in-memory storage |
| GET | `/api/randevular` | List in-memory appointments |
| GET | `/api/randevular/uygun-saatler?poliklinikId=1&doktorId=101&tarih=YYYY-MM-DD` | Get available times |
| POST | `/api/randevular` | Create an appointment |
| DELETE | `/api/randevular/{randevuId}` | Cancel an appointment |

Create request:

```json
{
  "randevuId": 10,
  "hastaId": 510,
  "poliklinikId": 1,
  "doktorId": 101,
  "tarih": "2026-11-26",
  "saat": "09:00"
}
```

Invalid input returns `400`, unsupported media type `415`, oversized request
`413`, conflicting appointment `409`, missing appointment `404`, and unsupported
method `405`. CORS preflight is limited to the configured origins, API methods,
and the `Content-Type` request header.

## PHP API client

`servis.ApiBaglantiServisi` also provides client methods for the existing PHP
module endpoints, relative to the configured base URL:

| Method | PHP endpoint | Purpose |
|---|---|---|
| GET | `api/hasta_listele.php` | List patients |
| GET | `api/randevu_listele.php` | List appointments |
| POST (form) | `api/hasta_kaydet.php` | Create or update a patient |
| POST (form) | `api/randevu_durum.php` | Update appointment status |

These methods return the complete `HttpResponse`, so callers must inspect its
status code and response body. The standalone Java server does not yet invoke
this client. Appointment creation and available-time lookup remain in-memory
operations; the PHP module currently has no matching endpoints for them.
