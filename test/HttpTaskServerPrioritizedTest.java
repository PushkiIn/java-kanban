import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import enums.Status;
import manager.InMemoryTaskManager;
import manager.TaskManager;
import model.Epic;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.HttpTaskServer;
import util.Managers;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpTaskServerPrioritizedTest {
    private final TaskManager manager = new InMemoryTaskManager();
    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = Managers.getGson();
    private final HttpTaskServer taskServer = new HttpTaskServer(manager);

    public HttpTaskServerPrioritizedTest() throws IOException {
    }

    @BeforeEach
    public void setUp() {
        manager.deleteTasks();
        manager.deleteSubtasks();
        manager.deleteEpics();
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void testGetPrioritizedEmpty() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Type taskListType = new TypeToken<List<Task>>() {
        }.getType();
        List<Task> prioritizedTasks = gson.fromJson(response.body(), taskListType);

        assertTrue(prioritizedTasks.isEmpty(), "Список задач по приоритету должен быть пуст");
    }

    @Test
    public void testPrioritizedMixedTaskTypes() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "desc");
        int epicId = postEpic(epic);

        Subtask subtask = new Subtask("Subtask", "desc", Status.NEW, LocalDateTime.now().plusMinutes(30), Duration.ofMinutes(15), epicId);
        Task task = new Task("Task", "desc", Status.NEW, LocalDateTime.now(), Duration.ofMinutes(20));

        postSubtask(subtask);
        postTask(task);

        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Type taskListType = new TypeToken<List<Task>>() {
        }.getType();
        List<Task> prioritizedTasks = gson.fromJson(response.body(), taskListType);

        assertEquals(2, prioritizedTasks.size());
        assertEquals(task.getName(), prioritizedTasks.get(0).getName(), "Task должен идти первым");
        assertEquals(subtask.getName(), prioritizedTasks.get(1).getName(), "Subtask должен идти вторым");
    }

    private int postEpic(Epic epic) throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/epics/");
        String epicJson = gson.toJson(epic);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();
        client.send(request, HttpResponse.BodyHandlers.ofString());

        return getEpics().stream()
                .filter(t -> t.getName().equals(epic.getName())
                        && t.getDescription().equals(epic.getDescription()))
                .map(Epic::getId)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Созданный эпик не найден"));
    }

    private int postSubtask(Subtask subtask) throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/subtasks");
        String subtaskJson = gson.toJson(subtask);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();
        client.send(request, HttpResponse.BodyHandlers.ofString());

        return getSubtasks().stream()
                .filter(t -> t.getName().equals(subtask.getName())
                        && t.getDescription().equals(subtask.getDescription()))
                .map(Task::getId)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Созданная задача не найдена"));
    }

    private List<Subtask> getSubtasks() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "GET /subtasks должен возвращать 200");
        Subtask[] subtasksArray = gson.fromJson(response.body(), Subtask[].class);
        return Arrays.asList(subtasksArray);
    }

    private int postTask(Task task) throws IOException, InterruptedException {
        String taskJson = gson.toJson(task);
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();
        client.send(request, HttpResponse.BodyHandlers.ofString());

        return getTasks().stream()
                .filter(t -> t.getName().equals(task.getName())
                        && t.getDescription().equals(task.getDescription()))
                .map(Task::getId)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Созданная задача не найдена"));
    }

    private List<Task> getTasks() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "GET /tasks должен возвращать 200");
        Task[] tasksArray = gson.fromJson(response.body(), Task[].class);
        return Arrays.asList(tasksArray);
    }

    private Task getTaskById(int id) throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks/" + id);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        return gson.fromJson(response.body(), Task.class);
    }

    private List<Epic> getEpics() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/epics/");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "GET /tasks должен возвращать 200");
        Epic[] epicsArray = gson.fromJson(response.body(), Epic[].class);
        return Arrays.asList(epicsArray);
    }
}