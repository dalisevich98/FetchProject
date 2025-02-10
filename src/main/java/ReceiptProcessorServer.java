import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import main.java.Item;
import main.java.Receipt;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ReceiptProcessorServer {
    private static final int PORT = 8080;
    private static final Map<String, Integer> receiptStore = new ConcurrentHashMap<>();
    private static final Gson gson = new Gson();

    static class ProcessReceiptHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
                return;
            }

            String requestBody = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining("\n"));

            Receipt receipt = gson.fromJson(requestBody, Receipt.class);
            String id = UUID.randomUUID().toString();
            int points = calculatePoints(receipt);
            receiptStore.put(id, points);

            sendResponse(exchange, 200, gson.toJson(Collections.singletonMap("id", id)));
        }
    }

    static class GetPointsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                sendResponse(exchange, 405, "{\"error\":\"Method Not Allowed\"}");
                return;
            }

            String path = exchange.getRequestURI().getPath();
            String[] parts = path.split("/");
            if (parts.length < 3) {
                sendResponse(exchange, 400, "{\"error\":\"Invalid request\"}");
                return;
            }

            String id = parts[2];
            Integer points = receiptStore.get(id);
            if (points == null) {
                sendResponse(exchange, 404, "{\"error\":\"Receipt not found\"}");
            } else {
                sendResponse(exchange, 200, gson.toJson(Collections.singletonMap("points", points)));
            }
        }
    }

    private static int calculatePoints(Receipt receipt) {
        int points = 0;

        // One point for each alphanumeric character in retailer name
        points += receipt.getRetailer().replaceAll("[^a-zA-Z0-9]", "").length();

        // 50 points if the total is a round dollar amount
        double total = Double.parseDouble(receipt.getTotal());
        if (total == Math.floor(total)) points += 50;

        // 25 points if the total is a multiple of 0.25
        if (total % 0.25 == 0) points += 25;

        // 5 points for every two items on the receipt
        points += (receipt.getItems().size() / 2) * 5;

        // Points based on item descriptions
        for (Item item : receipt.getItems()) {
            String desc = item.getShortDescription().trim();
            if (desc.length() % 3 == 0) {
                double itemPrice = Double.parseDouble(item.getPrice());
                points += (int) Math.ceil(itemPrice * 0.2);
            }
        }

        // 6 points if the day of purchase is odd
        int day = LocalDate.parse(receipt.getPurchaseDate()).getDayOfMonth();
        if (day % 2 == 1) points += 6;

        // 10 points if purchase time is between 2:00pm and 4:00pm
        LocalTime time = LocalTime.parse(receipt.getPurchaseTime(), DateTimeFormatter.ofPattern("HH:mm"));
        if (time.isAfter(LocalTime.of(14, 0)) && time.isBefore(LocalTime.of(16, 0))) {
            points += 10;
        }

        return points;
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.sendResponseHeaders(statusCode, response.getBytes(StandardCharsets.UTF_8).length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes(StandardCharsets.UTF_8));
        }
    }

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/receipts/process", new ProcessReceiptHandler());
        server.createContext("/receipts/", new GetPointsHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port " + PORT);
    }
}
