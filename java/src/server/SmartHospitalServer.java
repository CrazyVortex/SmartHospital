package server;

import com.sun.net.httpserver.HttpServer;
import controller.HealthController;
import controller.RandevuController;
import servis.RandevuKontrolServisi;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SmartHospitalServer {
    private static final int DEFAULT_PORT = 8081;
    private static final int WORKER_COUNT = 4;

    private SmartHospitalServer() {
    }

    public static void main(String[] args) throws IOException {
        int port = readPort();
        Set<String> allowedOrigins = RandevuController.parseAllowedOrigins(
                System.getenv("SMART_HOSPITAL_ALLOWED_ORIGINS"));
        InetAddress loopbackAddress = InetAddress.getByName("127.0.0.1");
        HttpServer server;
        try {
            server = HttpServer.create(new InetSocketAddress(loopbackAddress, port), 0);
        } catch (BindException exception) {
            throw new IOException("127.0.0.1:" + port + " portu baska bir islem tarafindan kullaniliyor.",
                    exception);
        }
        ExecutorService executor = Executors.newFixedThreadPool(WORKER_COUNT);
        server.setExecutor(executor);
        server.createContext("/api/health", new HealthController());
        server.createContext("/api/randevular",
                new RandevuController(RandevuKontrolServisi.ornekServisOlustur(), allowedOrigins));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("SmartHospital Java API icin kapatma istegi alindi.");
            server.stop(1);
            executor.shutdown();
            try {
                if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException exception) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            System.out.println("SmartHospital Java API duzgun sekilde kapatildi.");
        }, "smart-hospital-shutdown"));

        server.start();
        System.out.println("SmartHospital Java API BASLATILDI: http://127.0.0.1:" + port
                + " (health: /api/health, CORS kaynak sayisi: " + allowedOrigins.size()
                + ", depolama: bellek ici; MySQL baglantisi yok). Ctrl+C ile duzgun kapatabilirsiniz.");
    }

    private static int readPort() {
        String configuredPort = System.getenv("SMART_HOSPITAL_PORT");
        if (configuredPort == null || configuredPort.trim().isEmpty()) {
            return DEFAULT_PORT;
        }
        try {
            int port = Integer.parseInt(configuredPort.trim());
            if (port < 1 || port > 65535) {
                throw new IllegalArgumentException("SMART_HOSPITAL_PORT 1-65535 arasinda olmali.");
            }
            return port;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("SMART_HOSPITAL_PORT gecerli bir port olmali.");
        }
    }
}
