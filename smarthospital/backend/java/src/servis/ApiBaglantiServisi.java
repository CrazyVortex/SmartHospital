package servis;

import model.Muayene;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ApiBaglantiServisi {

    private static final String TEMEL_URL = "http://localhost/smarthospital/backend/php/api/";
    private final HttpClient istemci = HttpClient.newHttpClient();

    private String getIstegi(String uc) throws IOException, InterruptedException {
        HttpRequest istek = HttpRequest.newBuilder()
                .uri(URI.create(TEMEL_URL + uc))
                .GET()
                .build();
        HttpResponse<String> cevap = istemci.send(istek, HttpResponse.BodyHandlers.ofString());
        return cevap.body();
    }

    private String postIstegi(String uc, String json) throws IOException, InterruptedException {
        HttpRequest istek = HttpRequest.newBuilder()
                .uri(URI.create(TEMEL_URL + uc))
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> cevap = istemci.send(istek, HttpResponse.BodyHandlers.ofString());
        return cevap.body();
    }

    // JSON string alanlarındaki özel ve kontrol karakterlerini kaçırır.
    private String kacis(String metin) {
        if (metin == null) {
            return "";
        }

        StringBuilder sonuc = new StringBuilder(metin.length() + 16);
        for (int i = 0; i < metin.length(); i++) {
            char karakter = metin.charAt(i);
            switch (karakter) {
                case '"': sonuc.append("\\\""); break;
                case '\\': sonuc.append("\\\\"); break;
                case '\b': sonuc.append("\\b"); break;
                case '\f': sonuc.append("\\f"); break;
                case '\n': sonuc.append("\\n"); break;
                case '\r': sonuc.append("\\r"); break;
                case '\t': sonuc.append("\\t"); break;
                default:
                    if (karakter < 0x20) {
                        sonuc.append(String.format("\\u%04x", (int) karakter));
                    } else {
                        sonuc.append(karakter);
                    }
            }
        }
        return sonuc.toString();
    }

    public String dashboardOzetGetir() throws IOException, InterruptedException {
        return getIstegi("dashboard/dashboard_ozet.php");
    }

    public String yogunlukAnaliziGetir() throws IOException, InterruptedException {
        return getIstegi("dashboard/yogunluk_analiz.php");
    }

    public String muayeneGecmisiGetir(int randevuId) throws IOException, InterruptedException {
        return getIstegi("examinations/muayene_gecmis.php?randevu_id=" + randevuId);
    }

    public String muayeneKaydet(Muayene m) throws IOException, InterruptedException {
        String json = "{\"randevu_id\":" + m.getRandevuId()
                + ",\"sikayet\":\"" + kacis(m.getSikayet()) + "\""
                + ",\"tani\":\"" + kacis(m.getTani()) + "\""
                + ",\"recete\":\"" + kacis(m.getRecete()) + "\"}";
        return postIstegi("examinations/muayene_kaydet.php", json);
    }
}

