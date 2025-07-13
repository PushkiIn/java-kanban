package server.handlers;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class BaseHttpHandler {
    protected void sendText(HttpExchange exchange, String text, int rCode) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(rCode, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    protected void sendNotFound(HttpExchange exchange, int rCode) throws IOException {
        exchange.sendResponseHeaders(rCode, 0);
        exchange.close();
    }

    protected void sendHasOverlaps(HttpExchange exchange, int rCode) throws IOException {
        exchange.sendResponseHeaders(rCode, 0);
        exchange.close();
    }

    protected void sendServerError(HttpExchange exchange, int rCode) throws IOException {
        exchange.sendResponseHeaders(rCode, 0);
        exchange.close();
    }

    protected String getBody(HttpExchange exchange) throws IOException {
        InputStream inputStream = exchange.getRequestBody();
        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }
}