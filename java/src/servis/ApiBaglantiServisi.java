package servis;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;

public class ApiBaglantiServisi {
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);

    private final URI baseUri;
    private final HttpClient httpClient;

    public ApiBaglantiServisi(String baseUrl) {
        this(URI.create(ensureTrailingSlash(baseUrl)), HttpClient.newHttpClient());
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

    private static String ensureTrailingSlash(String baseUrl) {
        Objects.requireNonNull(baseUrl, "baseUrl cannot be null");
        return baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
    }
}
