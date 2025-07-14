import com.google.gson.Gson;
import enums.Status;
import manager.InMemoryTaskManager;
import manager.TaskManager;
import model.Task;
import org.junit.jupiter.api.*;

import server.HttpTaskServer;
import util.Managers;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerTasksTest {
    private final TaskManager manager = new InMemoryTaskManager();
    private final HttpTaskServer taskServer = new HttpTaskServer(manager);
    private final Gson gson = Managers.getGson();
    private final HttpClient client = HttpClient.newHttpClient();
    private final URI tasksUrl = URI.create("http://localhost:8080/tasks");

    public HttpTaskManagerTasksTest() throws IOException {
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
    public void testGetAllTasks() throws IOException, InterruptedException {
        Task task1 = new Task("Task 1", "Desc 1", Status.NEW, LocalDateTime.now(), Duration.ofMinutes(10));
        Task task2 = new Task("Task 2", "Desc 2", Status.NEW, LocalDateTime.now().plusMinutes(15), Duration.ofMinutes(10));
        manager.createTask(task1);
        manager.createTask(task2);

        HttpRequest request = HttpRequest.newBuilder().uri(tasksUrl).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Task[] tasks = gson.fromJson(response.body(), Task[].class);
        assertEquals(2, tasks.length, "Количество задач должно быть 2");
    }

    @Test
    public void testGetTaskById() throws IOException, InterruptedException {
        Task task = new Task("Task 1", "Desc 1", Status.NEW, LocalDateTime.now(), Duration.ofMinutes(10));
        int id = postTask(task);

        URI url = URI.create("http://localhost:8080/tasks/" + id);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Task returned = gson.fromJson(response.body(), Task.class);
        assertEquals(task, returned, "Задачи должны быть идентичными");
    }


    @Test
    public void testUpdateTask() throws IOException, InterruptedException {
        Task task = new Task("Test 2", "Testing task 2",
                Status.NEW, LocalDateTime.now(), Duration.ofMinutes(5));
        int taskId = postTask(task);
        task.setId(taskId);
        task.setName("Другое имя задачи");
        String taskJson = gson.toJson(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(tasksUrl)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        Task updated = manager.getTaskById(taskId);
        assertNotNull(updated, "Задача не найдена");
        assertEquals("Другое имя задачи", updated.getName(), "Имя задачи не обновилось");

        List<Task> tasksFromManager = getTasks();
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
    }

    @Test
    public void testDeleteAllTask() throws IOException, InterruptedException {
        Task task1 = new Task("Task 1", "Desc 1", Status.NEW, LocalDateTime.now(), Duration.ofMinutes(10));
        Task task2 = new Task("Task 2", "Desc 2", Status.NEW, LocalDateTime.now().plusMinutes(15), Duration.ofMinutes(10));
        postTask(task1);
        postTask(task2);

        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .uri(tasksUrl)
                .DELETE()
                .build();
        HttpResponse<String> deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, deleteResponse.statusCode(), "Удаление задач не прошло");

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(tasksUrl)
                .GET()
                .build();
        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, getResponse.statusCode(), "Не удалось получить задачи после удаления");
        Task[] tasks = gson.fromJson(getResponse.body(), Task[].class);
        assertEquals(0, tasks.length, "Количество задач должно быть 0 после удаления");
    }

    @Test
    void testDeleteTaskById() throws IOException, InterruptedException {
        Task task1 = new Task("Task 1", "Desc 1", Status.NEW, LocalDateTime.now(), Duration.ofMinutes(10));
        int taskId = postTask(task1);

        URI url = URI.create("http://localhost:8080/tasks/" + taskId);
        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();
        HttpResponse<String> deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, deleteResponse.statusCode(), "Удаление задач не прошло");

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(tasksUrl)
                .GET()
                .build();
        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, getResponse.statusCode(), "Не удалось получить задачи после удаления");
        Task[] tasks = gson.fromJson(getResponse.body(), Task[].class);
        assertEquals(0, tasks.length, "Количество задач должно быть 0 после удаления");
    }

    @Test
    public void testAddTask() throws IOException, InterruptedException {
        Task task = new Task("Test 2", "Testing task 2",
                Status.NEW, LocalDateTime.now(), Duration.ofMinutes(5));
        String taskJson = gson.toJson(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Task> tasksFromManager = manager.getTasks();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Test 2", tasksFromManager.get(0).getName(), "Некорректное имя задачи");
    }

    private int postTask(Task task) throws IOException, InterruptedException {
        String taskJson = gson.toJson(task);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(tasksUrl)
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
        HttpRequest request = HttpRequest.newBuilder()
                .uri(tasksUrl)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "GET /tasks должен возвращать 200");
        Task[] tasksArray = gson.fromJson(response.body(), Task[].class);
        return Arrays.asList(tasksArray);
    }
}