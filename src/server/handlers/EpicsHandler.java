package server.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import model.Epic;
import model.Subtask;
import util.Managers;

import java.io.IOException;
import java.util.List;

public class EpicsHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager manager;
    private final Gson gson;

    public EpicsHandler(TaskManager manager) {
        this.manager = manager;
        gson = Managers.getGson();
    }

    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String[] pathParts = exchange.getRequestURI().getPath().split("/");

        try {
            switch (method) {
                case "GET":
                    handleGet(exchange, pathParts);
                    break;
                case "POST":
                    handlePost(exchange, pathParts);
                    break;
                case "DELETE":
                    handleDelete(exchange, pathParts);
                    break;
                default:
                    exchange.sendResponseHeaders(405, 0); // Метод не поддерживается
                    exchange.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendServerError(exchange, 404);
        }
    }

    private void handleGet(HttpExchange exchange, String[] pathParts) throws IOException {
        if (pathParts.length == 2) {
            List<Epic> epics = manager.getEpics();
            sendText(exchange, gson.toJson(epics), 200);
        } else if (pathParts.length == 3) {
            try {
                int id = Integer.parseInt(pathParts[2]);
                Epic epic = manager.getEpicById(id);
                if (epic != null) {
                    sendText(exchange, gson.toJson(epic), 200);
                } else {
                    sendNotFound(exchange, 404);
                }
            } catch (NumberFormatException e) {
                sendNotFound(exchange, 404);
            }
        } else if (pathParts.length == 4 && pathParts[3].equals("subtasks")) {
            try {
                int id = Integer.parseInt(pathParts[2]);
                List<Subtask> epicSubtasks = manager.getEpicSubTasks(id);
                sendText(exchange, gson.toJson(epicSubtasks), 200);
            } catch (NumberFormatException e) {
                sendNotFound(exchange, 404);
            }
        } else {
            sendNotFound(exchange, 404);
        }
    }

    private void handlePost(HttpExchange exchange, String[] pathParts) throws IOException {
        if (pathParts.length == 2) {
            String body = getBody(exchange);
            Epic epic = gson.fromJson(body, Epic.class);

            if (epic.getId() == 0) {
                manager.createEpic(epic);
                sendText(exchange, "Новый эпик создан", 201);
            } else {
                if (manager.getEpicById(epic.getId()) != null) {
                    manager.updateTask(epic);
                    sendText(exchange, "Задача обновлена", 201);
                } else {
                    sendNotFound(exchange, 404);
                }
            }
        } else {
            sendNotFound(exchange, 404);
        }
    }

    private void handleDelete(HttpExchange exchange, String[] pathParts) throws IOException {
        if (pathParts.length == 2) {
            manager.deleteEpics();
            sendText(exchange, "Все эпики удалены", 200);
        } else if (pathParts.length == 3) {
            try {
                int id = Integer.parseInt(pathParts[2]);
                manager.deleteEpicById(id);
                sendText(exchange, "Эпик удален", 200);
            } catch (NumberFormatException e) {
                sendNotFound(exchange, 404);
            }
        } else {
            sendNotFound(exchange, 404);
        }
    }
}