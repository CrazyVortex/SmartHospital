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
import java.util.Objects;
import java.util.StringJoiner;

public class ApiBaglantiServisi {
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);

    private final URI baseUri;
    private final HttpClient httpClient;

    public ApiBaglantiServisi(String baseUrl) {
        this(URI.create(ensureTrailingSlash(baseUrl)), HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build());
    }

    public ApiBaglantiServisi(URI baseUri, HttpClient httpClient) {
        this.baseUri = Objects.requireNonNull(baseUri, "baseUri cannot be null");
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient cannot be null");
    }

    public HttpResponse<String> getJson(String endpoint)
            throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(resolveEndpoint(endpoint))
                .timeout(REQUEST_TIMEOUT)
                .header("Accept", "application/json")
                .GET()
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    public HttpResponse<String> postJson(String endpoint, String jsonBody)
            throws IOException, InterruptedException {
        Objects.requireNonNull(jsonBody, "jsonBody cannot be null");

        HttpRequest request = HttpRequest.newBuilder(resolveEndpoint(endpoint))
                .timeout(REQUEST_TIMEOUT)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    public HttpResponse<String> getPatients() throws IOException, InterruptedException {
        return getJson("api/hasta_listele.php");
    }

    public HttpResponse<String> getAppointments() throws IOException, InterruptedException {
        return getJson("api/randevu_listele.php");
    }

    public HttpResponse<String> savePatient(Integer patientId,
                                             String tcNo,
                                             String firstName,
                                             String lastName,
                                             String birthDate,
                                             String gender,
                                             String phone,
                                             String bloodGroup)
            throws IOException, InterruptedException {
        Map<String, String> fields = new LinkedHashMap<>();
        if (patientId != null) {
            fields.put("hasta_id", patientId.toString());
        }
        fields.put("tc_no", formValue(tcNo));
        fields.put("ad", formValue(firstName));
        fields.put("soyad", formValue(lastName));
        fields.put("dogum_tarihi", formValue(birthDate));
        fields.put("cinsiyet", formValue(gender));
        fields.put("telefon", formValue(phone));
        fields.put("kan_grubu", formValue(bloodGroup));
        return postForm("api/hasta_kaydet.php", fields);
    }

    public HttpResponse<String> updateAppointmentStatus(int appointmentId,
                                                        String status,
                                                        String notes)
            throws IOException, InterruptedException {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("randevu_id", Integer.toString(appointmentId));
        fields.put("durum", formValue(status));
        fields.put("notlar", formValue(notes));
        return postForm("api/randevu_durum.php", fields);
    }

    private HttpResponse<String> postForm(String endpoint, Map<String, String> fields)
            throws IOException, InterruptedException {
        StringJoiner body = new StringJoiner("&");
        for (Map.Entry<String, String> field : fields.entrySet()) {
            String name = URLEncoder.encode(field.getKey(), StandardCharsets.UTF_8);
            String value = URLEncoder.encode(field.getValue(), StandardCharsets.UTF_8);
            body.add(name + "=" + value);
        }

        HttpRequest request = HttpRequest.newBuilder(resolveEndpoint(endpoint))
                .timeout(REQUEST_TIMEOUT)
                .header("Accept", "application/json")
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private URI resolveEndpoint(String endpoint) {
        Objects.requireNonNull(endpoint, "endpoint cannot be null");
        URI endpointUri = URI.create(endpoint);
        if (endpointUri.isAbsolute() || endpointUri.getRawAuthority() != null) {
            throw new IllegalArgumentException("Endpoint must be relative to the configured base URI.");
        }

        URI resolvedUri = baseUri.resolve(endpointUri);
        if (!Objects.equals(baseUri.getScheme(), resolvedUri.getScheme())
                || !Objects.equals(baseUri.getRawAuthority(), resolvedUri.getRawAuthority())) {
            throw new IllegalArgumentException("Endpoint must use the configured API origin.");
        }
        return resolvedUri;
    }

    private String formValue(String value) {
        return value == null ? "" : value;
    }

    private static String ensureTrailingSlash(String baseUrl) {
        Objects.requireNonNull(baseUrl, "baseUrl cannot be null");
        return baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
    }
}
