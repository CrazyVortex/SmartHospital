package controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import util.JsonCodec;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class HealthController implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
            exchange.getResponseHeaders().set("Cache-Control", "no-store");
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendError(exchange, 405, "METHOD_NOT_ALLOWED",
                        "Bu HTTP yontemi health endpoint'i icin desteklenmiyor.");
                return;
            }

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("status", "ok");
            data.put("service", "SmartHospital Java API");
            data.put("storage", "in-memory");
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("data", data);
            sendJson(exchange, 200, response);
        } finally {
            exchange.close();
        }
    }

    private void sendError(HttpExchange exchange, int statusCode, String code, String message)
            throws IOException {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("code", code);
        error.put("message", message);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("error", error);
        sendJson(exchange, statusCode, response);
    }

    private void sendJson(HttpExchange exchange, int statusCode, Object value) throws IOException {
        byte[] response = JsonCodec.stringify(value).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, response.length);
        exchange.getResponseBody().write(response);
    }
}
