package server.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exception.TaskTimeConflictException;
import manager.TaskManager;
import model.Subtask;
import util.Managers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SubTasksHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager manager;
    private final Gson gson;

    public SubTasksHandler(TaskManager manager) {
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
        if (pathParts.length == 2 && pathParts[1].equals("subtasks")) {
            List<Subtask> subtasks = manager.getSubTasks();
            sendText(exchange, gson.toJson(subtasks), 200);
        } else if (pathParts.length == 3 && pathParts[1].equals("subtasks")) {
            try {
                int id = Integer.parseInt(pathParts[2]);
                Subtask subTask = manager.getSubTaskById(id);
                if (subTask != null) {
                    sendText(exchange, gson.toJson(subTask), 200);
                } else {
                    sendNotFound(exchange, 404);
                }
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
            Subtask subTask = gson.fromJson(body, Subtask.class);

            try {
                if (subTask.getId() == 0) {
                    manager.createSubTask(subTask);
                    sendText(exchange, "Новая подзадача создана", 201);
                } else {
                    if (manager.getSubTaskById(subTask.getId()) != null) {
                        manager.updateTask(subTask);
                        sendText(exchange, "Подзадача обновлена", 201);
                    } else {
                        sendNotFound(exchange, 404);
                    }
                }
            } catch (TaskTimeConflictException e) {
                sendHasOverlaps(exchange, 406);
            }

        } else {
            sendNotFound(exchange, 404);
        }
    }

    private void handleDelete(HttpExchange exchange, String[] pathParts) throws IOException {
        if (pathParts.length == 2 && pathParts[1].equals("subtasks")) {
            manager.deleteSubtasks();
            sendText(exchange, "Все подзадачи удалены", 200);
        } else if (pathParts.length == 3 && pathParts[1].equals("subtasks")) {
            try {
                int id = Integer.parseInt(pathParts[2]);
                manager.deleteSubTaskById(id);
                sendText(exchange, "Подзадача удалена", 200);
            } catch (NumberFormatException e) {
                sendNotFound(exchange, 404);
            }
        } else {
            sendNotFound(exchange, 404);
        }
    }
}