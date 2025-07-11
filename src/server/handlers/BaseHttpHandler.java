package server.handlers;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
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
}