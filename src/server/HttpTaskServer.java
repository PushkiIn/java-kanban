package server;

import com.sun.net.httpserver.HttpServer;
import manager.TaskManager;
import server.handlers.*;
import util.Managers;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private final HttpServer server;
    private final TaskManager taskManager;

    public HttpTaskServer() throws IOException {
        taskManager = Managers.getDefault();

        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        initializeContext();
    }

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.taskManager = taskManager;

        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        initializeContext();
    }

    public void initializeContext() {
        server.createContext("/tasks", new TasksHandler(taskManager));
        server.createContext("/subtasks", new SubTasksHandler(taskManager));
        server.createContext("/epics", new EpicsHandler(taskManager));
        server.createContext("/history", new HistoryHandler(taskManager));
        server.createContext("/prioritized", new PrioritizedHandler(taskManager));
    }

    public void start() {
        System.out.println("HTTP-сервер запущен на порту " + PORT);
        server.start();
    }

    public void stop() {
        server.stop(0);
    }

    public static void main(String[] args) throws IOException {
        new HttpTaskServer().start();
    }
}