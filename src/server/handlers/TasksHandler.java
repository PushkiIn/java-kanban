package server.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exception.TaskTimeConflictException;
import manager.TaskManager;
import model.Task;
import util.Managers;

import java.io.IOException;
import java.util.List;

public class TasksHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager manager;
    private final Gson gson;

    public TasksHandler(TaskManager manager) {
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
                    exchange.sendResponseHeaders(405, 0);
                    exchange.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendServerError(exchange, 500);
        }
    }

    private void handleGet(HttpExchange exchange, String[] pathParts) throws IOException {
        if (pathParts.length == 2 && pathParts[1].equals("tasks")) {
            List<Task> tasks = manager.getTasks();
            sendText(exchange, gson.toJson(tasks), 200);
        } else if (pathParts.length == 3 && pathParts[1].equals("tasks")) {
            try {
                int id = Integer.parseInt(pathParts[2]);
                Task task = manager.getTaskById(id);
                if (task != null) {
                    sendText(exchange, gson.toJson(task), 200);
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

    private void handlePost(HttpExchange exchange, String[] pathParts) throws IOException, TaskTimeConflictException {
        if (pathParts.length == 2) {
            String body = getBody(exchange);
            Task task = gson.fromJson(body, Task.class);

            try {
                if (task.getId() == 0) {
                    manager.createTask(task);
                    sendText(exchange, "Новая задача создана", 201);
                } else {
                    if (manager.getTaskById(task.getId()) != null) {
                        manager.updateTask(task);
                        sendText(exchange, "Задача обновлена", 201);
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
        if (pathParts.length == 2 && pathParts[1].equals("tasks")) {
            manager.deleteTasks();
            sendText(exchange, "Все задачи удалены", 200);
        } else if (pathParts.length == 3 && pathParts[1].equals("tasks")) {
            try {
                int id = Integer.parseInt(pathParts[2]);
                manager.deleteTaskById(id);
                sendText(exchange, "Задача удалена", 200);
            } catch (NumberFormatException e) {
                sendNotFound(exchange, 404);
            }
        } else {
            sendNotFound(exchange, 404);
        }
    }
}