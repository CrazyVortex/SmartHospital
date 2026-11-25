package servis;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

public class ApiBaglantiServisi {
    private final String temelUrl;
    private final HttpClient httpClient;

    public ApiBaglantiServisi(String temelUrl) {
        if (temelUrl.endsWith("/")) {
            this.temelUrl = temelUrl.substring(0, temelUrl.length() - 1);
        } else {
            this.temelUrl = temelUrl;
        }

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public String hastaListeleJson() throws IOException, InterruptedException {
        return getIstegiGonder("/api/hasta_listele.php");
    }

    public String randevuListeleJson() throws IOException, InterruptedException {
        return getIstegiGonder("/api/randevu_listele.php");
    }

    public String hastaKaydetJson(String tcNo, String ad, String soyad,
                                  String dogumTarihi, String cinsiyet,
                                  String telefon, String kanGrubu) throws IOException, InterruptedException {
        Map<String, String> alanlar = new LinkedHashMap<>();
        alanlar.put("tc_no", tcNo);
        alanlar.put("ad", ad);
        alanlar.put("soyad", soyad);
        alanlar.put("dogum_tarihi", dogumTarihi);
        alanlar.put("cinsiyet", cinsiyet);
        alanlar.put("telefon", telefon);
        alanlar.put("kan_grubu", kanGrubu != null ? kanGrubu : "");

        return postFormIstegiGonder("/api/hasta_kaydet.php", alanlar);
    }

    protected String getIstegiGonder(String endpointYolu) throws IOException, InterruptedException {
        String tamUrl = temelUrl + (endpointYolu.startsWith("/") ? endpointYolu : "/" + endpointYolu);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(tamUrl))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    protected String postFormIstegiGonder(String endpointYolu, Map<String, String> alanlar)
            throws IOException, InterruptedException {
        String tamUrl = temelUrl + (endpointYolu.startsWith("/") ? endpointYolu : "/" + endpointYolu);

        StringJoiner formGovdesi = new StringJoiner("&");
        for (Map.Entry<String, String> entry : alanlar.entrySet()) {
            String anahtar = URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8);
            String deger = URLEncoder.encode(entry.getValue() != null ? entry.getValue() : "", StandardCharsets.UTF_8);
            formGovdesi.add(anahtar + "=" + deger);
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(tamUrl))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(formGovdesi.toString()))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }
}