package com.example.weatherapp;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", new FormHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("サーバーがポート8080で起動しました");
    }

    static class FormHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                handleGetRequest(exchange);
            } else if ("POST".equals(exchange.getRequestMethod())) {
                handlePostRequest(exchange);
            }
        }

        private void handleGetRequest(HttpExchange exchange) throws IOException {
            String response = "<html><body>" +
                    "<h1>給水通知</h1>" +
                    "<form method='POST' action='/'>" +
                    "気温を入力してください: <input type='text' name='temperature'>" +
                    "<input type='submit' value='送信'>" +
                    "</form>" +
                    "</body></html>";
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

        private void handlePostRequest(HttpExchange exchange) throws IOException {
            StringBuilder requestBody = new StringBuilder();
            try (InputStream is = exchange.getRequestBody();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                int ch;
                while ((ch = reader.read()) != -1) {
                    requestBody.append((char) ch);
                }
            }

            String temperatureParam = URLDecoder.decode(requestBody.toString(), StandardCharsets.UTF_8.name());
            String temperature = temperatureParam.split("=")[1];
            String message = generateMessage(Double.parseDouble(temperature));

            String response = "<html><body>" +
                    "<h1>給水通知</h1>" +
                    "<p>気温: " + temperature + "°C</p>" +
                    "<p>" + message + "</p>" +
                    "<a href='/'>戻る</a>" +
                    "</body></html>";
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

        private String generateMessage(double temperature) {
            if (temperature >= 30.0) {
                return "暑いです！たくさんの水を飲んでください。";
            } else if (temperature >= 20.0) {
                return "暖かいです。水分補給を忘れずに。";
            } else {
                return "涼しいです。定期的に水を飲んでください。";
            }
        }
    }
}
