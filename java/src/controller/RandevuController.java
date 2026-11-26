package controller;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.Randevu;
import servis.RandevuKontrolServisi;
import util.JsonCodec;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class RandevuController implements HttpHandler {
    private static final int MAX_REQUEST_BYTES = 64 * 1024;
    private static final Set<String> CREATE_FIELDS =
            new LinkedHashSet<>(Arrays.asList(
                    "randevuId", "hastaId", "poliklinikId", "doktorId", "tarih", "saat"));

    private final RandevuKontrolServisi service;
    private final Set<String> allowedOrigins;

    public RandevuController(RandevuKontrolServisi service, Set<String> allowedOrigins) {
        if (service == null || allowedOrigins == null) {
            throw new IllegalArgumentException("Servis ve CORS kaynak listesi zorunludur.");
        }
        this.service = service;
        this.allowedOrigins = new LinkedHashSet<>();
        for (String origin : allowedOrigins) {
            this.allowedOrigins.add(normalizeOrigin(origin));
        }
    }

    public static Set<String> parseAllowedOrigins(String configuration) {
        Set<String> origins = new LinkedHashSet<>();
        if (configuration == null || configuration.trim().isEmpty()) {
            return origins;
        }
        for (String origin : configuration.split(",")) {
            if (!origin.trim().isEmpty()) {
                origins.add(normalizeOrigin(origin.trim()));
            }
        }
        return origins;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            applySecurityHeaders(exchange.getResponseHeaders());
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())
                    && allowedMethodsForPath(exchange.getRequestURI().getPath()) == null) {
                sendError(exchange, 404, "NOT_FOUND", "Istenen API yolu bulunamadi.");
                return;
            }
            if (!applyCors(exchange)) {
                return;
            }
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                handlePreflight(exchange);
                return;
            }

            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod().toUpperCase(Locale.ROOT);
            if ("/api/randevular".equals(path)) {
                handleCollection(exchange, method);
            } else if ("/api/randevular/uygun-saatler".equals(path)) {
                handleAvailableTimes(exchange, method);
            } else if (path.matches("/api/randevular/[0-9]+")) {
                handleAppointment(exchange, method, path);
            } else {
                sendError(exchange, 404, "NOT_FOUND", "Istenen API yolu bulunamadi.");
            }
        } catch (RequestException exception) {
            sendError(exchange, exception.statusCode, exception.code, exception.getMessage());
        } catch (IllegalStateException exception) {
            sendError(exchange, 409, "APPOINTMENT_CONFLICT", exception.getMessage());
        } catch (IllegalArgumentException exception) {
            sendError(exchange, 400, "INVALID_REQUEST", exception.getMessage());
        } catch (Exception exception) {
            exception.printStackTrace(System.err);
            sendError(exchange, 500, "INTERNAL_ERROR", "Beklenmeyen bir sunucu hatasi olustu.");
        } finally {
            exchange.close();
        }
    }

    private boolean applyCors(HttpExchange exchange) throws IOException {
        Headers headers = exchange.getResponseHeaders();
        headers.add("Vary", "Origin");
        String origin = exchange.getRequestHeaders().getFirst("Origin");
        if (origin == null) {
            return true;
        }

        String normalizedOrigin;
        try {
            normalizedOrigin = normalizeOrigin(origin);
        } catch (IllegalArgumentException exception) {
            sendError(exchange, 403, "ORIGIN_NOT_ALLOWED", "Bu kaynak icin CORS izni yok.");
            return false;
        }
        if (!allowedOrigins.contains(normalizedOrigin)) {
            sendError(exchange, 403, "ORIGIN_NOT_ALLOWED", "Bu kaynak icin CORS izni yok.");
            return false;
        }

        headers.set("Access-Control-Allow-Origin", normalizedOrigin);
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            String requestedMethod =
                    exchange.getRequestHeaders().getFirst("Access-Control-Request-Method");
            Set<String> pathMethods = allowedMethodsForPath(exchange.getRequestURI().getPath());
            if (requestedMethod == null || pathMethods == null
                    || !pathMethods.contains(requestedMethod.toUpperCase(Locale.ROOT))) {
                sendError(exchange, 403, "CORS_METHOD_NOT_ALLOWED",
                        "Istenen HTTP yontemine CORS izni yok.");
                return false;
            }

            String requestedHeaders =
                    exchange.getRequestHeaders().getFirst("Access-Control-Request-Headers");
            if (requestedHeaders != null && !onlyContentTypeRequested(requestedHeaders)) {
                sendError(exchange, 403, "CORS_HEADER_NOT_ALLOWED",
                        "Istenen HTTP basligina CORS izni yok.");
                return false;
            }
            headers.set("Access-Control-Allow-Methods",
                    String.join(", ", pathMethods) + ", OPTIONS");
            headers.set("Access-Control-Allow-Headers", "Content-Type");
            headers.set("Access-Control-Max-Age", "600");
        }
        return true;
    }

    private void handlePreflight(HttpExchange exchange) throws IOException {
        Set<String> pathMethods = allowedMethodsForPath(exchange.getRequestURI().getPath());
        exchange.getResponseHeaders().set("Allow", String.join(", ", pathMethods) + ", OPTIONS");
        exchange.sendResponseHeaders(204, -1);
    }

    private Set<String> allowedMethodsForPath(String path) {
        if ("/api/randevular".equals(path)) {
            return new LinkedHashSet<>(Arrays.asList("GET", "POST"));
        }
        if ("/api/randevular/uygun-saatler".equals(path)) {
            return new LinkedHashSet<>(Arrays.asList("GET"));
        }
        if (path.matches("/api/randevular/[0-9]+")) {
            return new LinkedHashSet<>(Arrays.asList("DELETE"));
        }
        return null;
    }

    private void handleCollection(HttpExchange exchange, String method) throws IOException {
        if ("GET".equals(method)) {
            List<Map<String, Object>> appointments = new ArrayList<>();
            for (Randevu appointment : service.randevulariGetir()) {
                appointments.add(toAppointmentJson(appointment));
            }
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("randevular", appointments);
            sendSuccess(exchange, 200, data);
            return;
        }
        if ("POST".equals(method)) {
            Map<String, Object> request = readJsonObject(exchange);
            rejectUnknownFields(request, CREATE_FIELDS);

            int appointmentId = requiredInteger(request, "randevuId");
            int patientId = requiredInteger(request, "hastaId");
            int clinicId = requiredInteger(request, "poliklinikId");
            int doctorId = requiredInteger(request, "doktorId");
            LocalDate date = requiredDate(request, "tarih");
            LocalTime time = requiredTime(request, "saat");

            Randevu appointment = service.randevuOlustur(
                    appointmentId, patientId, clinicId, doctorId, date, time);
            sendSuccess(exchange, 201, toAppointmentJson(appointment));
            return;
        }
        sendMethodNotAllowed(exchange, "GET, POST, OPTIONS");
    }

    private void handleAvailableTimes(HttpExchange exchange, String method) throws IOException {
        if (!"GET".equals(method)) {
            sendMethodNotAllowed(exchange, "GET, OPTIONS");
            return;
        }
        Map<String, String> query = parseQuery(exchange.getRequestURI());
        Set<String> expected = new LinkedHashSet<>(
                Arrays.asList("poliklinikId", "doktorId", "tarih"));
        rejectUnknownFields(query, expected);

        int clinicId = requiredQueryInteger(query, "poliklinikId");
        int doctorId = requiredQueryInteger(query, "doktorId");
        LocalDate date = parseDate(requiredQueryValue(query, "tarih"), "tarih");

        List<String> times = new ArrayList<>();
        for (LocalTime time : service.uygunSaatleriGetir(clinicId, doctorId, date)) {
            times.add(time.toString());
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("uygunSaatler", times);
        sendSuccess(exchange, 200, data);
    }

    private void handleAppointment(HttpExchange exchange, String method, String path)
            throws IOException {
        if (!"DELETE".equals(method)) {
            sendMethodNotAllowed(exchange, "DELETE, OPTIONS");
            return;
        }
        String idPart = path.substring("/api/randevular/".length());
        int appointmentId;
        try {
            appointmentId = Integer.parseInt(idPart);
        } catch (NumberFormatException exception) {
            throw new RequestException(400, "INVALID_ID", "Randevu ID gecerli degil.");
        }
        if (appointmentId <= 0) {
            throw new RequestException(400, "INVALID_ID", "Randevu ID pozitif olmalidir.");
        }
        try {
            service.randevuyuIptalEt(appointmentId);
        } catch (IllegalArgumentException exception) {
            throw new RequestException(404, "APPOINTMENT_NOT_FOUND",
                    "Iptal edilecek randevu bulunamadi.");
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", appointmentId);
        data.put("durum", RandevuKontrolServisi.DURUM_IPTAL);
        sendSuccess(exchange, 200, data);
    }

    private Map<String, Object> readJsonObject(HttpExchange exchange) throws IOException {
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (contentType == null
                || !contentType.split(";", 2)[0].trim().equalsIgnoreCase("application/json")) {
            throw new RequestException(
                    415, "UNSUPPORTED_MEDIA_TYPE", "Istek Content-Type application/json olmalidir.");
        }

        byte[] body;
        try (InputStream input = exchange.getRequestBody()) {
            body = input.readNBytes(MAX_REQUEST_BYTES + 1);
        }
        if (body.length > MAX_REQUEST_BYTES) {
            throw new RequestException(413, "REQUEST_TOO_LARGE",
                    "Istek govdesi izin verilen boyutu asiyor.");
        }
        Object parsed;
        try {
            parsed = JsonCodec.parse(new String(body, StandardCharsets.UTF_8));
        } catch (IllegalArgumentException exception) {
            throw new RequestException(400, "INVALID_JSON", exception.getMessage());
        }
        if (!(parsed instanceof Map<?, ?>)) {
            throw new RequestException(400, "INVALID_JSON", "JSON govdesi nesne olmalidir.");
        }

        Map<String, Object> request = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : ((Map<?, ?>) parsed).entrySet()) {
            request.put((String) entry.getKey(), entry.getValue());
        }
        return request;
    }

    private Map<String, String> parseQuery(URI uri) {
        Map<String, String> values = new LinkedHashMap<>();
        String rawQuery = uri.getRawQuery();
        if (rawQuery == null || rawQuery.isEmpty()) {
            return values;
        }
        for (String part : rawQuery.split("&")) {
            String[] pair = part.split("=", 2);
            String key = decodeQueryPart(pair[0]);
            String value = pair.length == 2 ? decodeQueryPart(pair[1]) : "";
            if (values.putIfAbsent(key, value) != null) {
                throw new IllegalArgumentException("Sorgu parametresi yinelenemez: " + key);
            }
        }
        return values;
    }

    private String decodeQueryPart(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
        } catch (Exception exception) {
            throw new IllegalArgumentException("Sorgu parametresi kodlamasi gecersiz.");
        }
    }

    private int requiredQueryInteger(Map<String, String> values, String name) {
        String value = requiredQueryValue(values, name);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(name + " pozitif bir tam sayi olmalidir.");
        }
    }

    private String requiredQueryValue(Map<String, String> values, String name) {
        String value = values.get(name);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " parametresi zorunludur.");
        }
        return value;
    }

    private int requiredInteger(Map<String, Object> request, String name) {
        Object value = request.get(name);
        if (!(value instanceof BigDecimal)) {
            throw new IllegalArgumentException(name + " pozitif bir tam sayi olmalidir.");
        }
        try {
            return ((BigDecimal) value).intValueExact();
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException(name + " pozitif bir tam sayi olmalidir.");
        }
    }

    private LocalDate requiredDate(Map<String, Object> request, String name) {
        Object value = request.get(name);
        if (!(value instanceof String) || ((String) value).trim().isEmpty()) {
            throw new IllegalArgumentException(name + " alani YYYY-MM-DD formatinda zorunludur.");
        }
        return parseDate((String) value, name);
    }

    private LocalDate parseDate(String value, String name) {
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException(name + " YYYY-MM-DD formatinda olmalidir.");
        }
    }

    private LocalTime requiredTime(Map<String, Object> request, String name) {
        Object value = request.get(name);
        if (!(value instanceof String) || ((String) value).trim().isEmpty()) {
            throw new IllegalArgumentException(name + " alani zorunludur.");
        }
        try {
            return LocalTime.parse((String) value);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException(name + " ISO-8601 saat formatinda olmalidir.");
        }
    }

    private void rejectUnknownFields(Map<String, ?> values, Set<String> allowed) {
        for (String field : values.keySet()) {
            if (!allowed.contains(field)) {
                throw new IllegalArgumentException("Bilinmeyen alan veya parametre: " + field);
            }
        }
    }

    private Map<String, Object> toAppointmentJson(Randevu appointment) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("randevuId", appointment.getId());
        value.put("hastaId", appointment.getHastaId());
        value.put("doktorId", appointment.getDoktorId());
        value.put("tarih", appointment.getTarih().toString());
        value.put("saat", appointment.getSaat().toString());
        value.put("durum", appointment.getDurum());
        return value;
    }

    private void sendSuccess(HttpExchange exchange, int statusCode, Object data)
            throws IOException {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("data", data);
        sendJson(exchange, statusCode, response);
    }

    private void sendError(HttpExchange exchange, int statusCode, String code, String message)
            throws IOException {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("code", code);
        error.put("message", message == null ? "Istek islenemedi." : message);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("error", error);
        sendJson(exchange, statusCode, response);
    }

    private void sendMethodNotAllowed(HttpExchange exchange, String allow) throws IOException {
        exchange.getResponseHeaders().set("Allow", allow);
        sendError(exchange, 405, "METHOD_NOT_ALLOWED", "Bu HTTP yontemi endpoint icin desteklenmiyor.");
    }

    private void sendJson(HttpExchange exchange, int statusCode, Object body) throws IOException {
        byte[] bytes = JsonCodec.stringify(body).getBytes(StandardCharsets.UTF_8);
        Headers headers = exchange.getResponseHeaders();
        headers.set("Content-Type", "application/json; charset=UTF-8");
        headers.set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        exchange.getResponseBody().write(bytes);
    }

    private void applySecurityHeaders(Headers headers) {
        headers.set("X-Content-Type-Options", "nosniff");
        headers.set("Cache-Control", "no-store");
    }

    private boolean onlyContentTypeRequested(String requestedHeaders) {
        for (String header : requestedHeaders.split(",")) {
            if (!"content-type".equals(header.trim().toLowerCase(Locale.ROOT))) {
                return false;
            }
        }
        return true;
    }

    private static String normalizeOrigin(String origin) {
        if (origin == null) {
            throw new IllegalArgumentException("CORS kaynagi bos olamaz.");
        }
        URI uri;
        try {
            uri = URI.create(origin);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("CORS kaynagi gecerli bir origin olmali.");
        }
        String scheme = uri.getScheme();
        String host = uri.getHost();
        if (scheme == null || host == null
                || (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))
                || uri.getRawUserInfo() != null
                || uri.getPort() > 65535
                || (uri.getRawAuthority() != null && uri.getRawAuthority().endsWith(":"))
                || (uri.getRawPath() != null
                    && !uri.getRawPath().isEmpty()
                    && !"/".equals(uri.getRawPath()))
                || uri.getRawQuery() != null || uri.getRawFragment() != null) {
            throw new IllegalArgumentException("CORS kaynagi yalnizca HTTP(S) origin olabilir.");
        }
        String normalizedHost = host.toLowerCase(Locale.ROOT);
        if (normalizedHost.indexOf(':') >= 0 && !normalizedHost.startsWith("[")) {
            normalizedHost = "[" + normalizedHost + "]";
        }
        StringBuilder normalized = new StringBuilder()
                .append(scheme.toLowerCase(Locale.ROOT))
                .append("://")
                .append(normalizedHost);
        int port = uri.getPort();
        if (port >= 0
                && !(("http".equalsIgnoreCase(scheme) && port == 80)
                    || ("https".equalsIgnoreCase(scheme) && port == 443))) {
            normalized.append(':').append(port);
        }
        return normalized.toString();
    }

    private static final class RequestException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        private final int statusCode;
        private final String code;

        private RequestException(int statusCode, String code, String message) {
            super(message);
            this.statusCode = statusCode;
            this.code = code;
        }
    }
}
