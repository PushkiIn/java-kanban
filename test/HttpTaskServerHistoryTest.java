import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import enums.Status;
import manager.InMemoryTaskManager;
import manager.TaskManager;
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

public class HttpTaskServerHistoryTest {
    private final TaskManager manager = new InMemoryTaskManager();
    private final HttpClient client = HttpClient.newHttpClient();
    private final Gson gson = Managers.getGson();
    private final HttpTaskServer taskServer = new HttpTaskServer(manager);

    public HttpTaskServerHistoryTest() throws IOException {
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
    public void testGetHistoryWithTasks() throws IOException, InterruptedException {
        Task task1 = new Task("Task 1", "Description 1", Status.NEW, LocalDateTime.now(), Duration.ofMinutes(30));
        Task task2 = new Task("Task 2", "Description 2", Status.IN_PROGRESS, LocalDateTime.now().plusHours(1), Duration.ofMinutes(45));

        int taskId1 = postTask(task1);
        int taskId2 = postTask(task2);

        getTaskById(taskId1);
        getTaskById(taskId2);

        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Type taskListType = new TypeToken<List<Task>>() {
        }.getType();
        List<Task> history = gson.fromJson(response.body(), taskListType);

        assertEquals(2, history.size(), "В истории должно быть 2 задачи");
        assertEquals(task1, history.get(0));
        assertEquals(task2, history.get(1));
    }

    @Test
    public void testGetEmptyHistory() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        Type taskListType = new TypeToken<List<Task>>() {
        }.getType();
        List<Task> history = gson.fromJson(response.body(), taskListType);

        assertTrue(history.isEmpty(), "История должна быть пустой");
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
}