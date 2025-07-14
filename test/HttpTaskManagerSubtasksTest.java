import com.google.gson.Gson;
import enums.Status;
import manager.InMemoryTaskManager;
import manager.TaskManager;
import model.Epic;
import model.Subtask;
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

public class HttpTaskManagerSubtasksTest {

    private final TaskManager manager = new InMemoryTaskManager();
    private final HttpTaskServer taskServer = new HttpTaskServer(manager);
    private final Gson gson = Managers.getGson();
    private final HttpClient client = HttpClient.newHttpClient();
    private final URI subtasksUrl = URI.create("http://localhost:8080/subtasks");

    public HttpTaskManagerSubtasksTest() throws IOException {
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
    public void testGetAllSubtasks() throws IOException, InterruptedException {
        int epicId = manager.createEpic(createEpic());
        Subtask subtask1 = new Subtask("SubTask 1", "Desc 1", Status.NEW, LocalDateTime.now(), Duration.ofMinutes(10), epicId);
        Subtask subtask2 = new Subtask("SUbTask 2", "Desc 2", Status.NEW, LocalDateTime.now().plusMinutes(15), Duration.ofMinutes(10), epicId);
        postSubtask(subtask1);
        postSubtask(subtask2);

        HttpRequest request = HttpRequest.newBuilder().uri(subtasksUrl).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Subtask[] subtasks = gson.fromJson(response.body(), Subtask[].class);
        assertEquals(2, subtasks.length, "Количество подзадач должно быть 2");
    }

    @Test
    public void testGetTaskById() throws IOException, InterruptedException {
        int epicId = manager.createEpic(createEpic());

        Subtask subtask = new Subtask("SubTask 1", "Desc 1", Status.NEW, LocalDateTime.now(), Duration.ofMinutes(10), epicId);
        int subtaskId = postSubtask(subtask);
        subtask.setId(subtaskId);

        URI url = URI.create("http://localhost:8080/subtasks/" + subtaskId);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Subtask returned = gson.fromJson(response.body(), Subtask.class);
        assertEquals(subtask, returned, "Задачи должны быть идентичными");
    }

    @Test
    public void testUpdateTask() throws IOException, InterruptedException {
        int epicId = manager.createEpic(createEpic());

        Subtask subtask = new Subtask("Test 2", "Testing task 2",
                Status.NEW, LocalDateTime.now(), Duration.ofMinutes(5), epicId);
        int subtaskId = postSubtask(subtask);
        subtask.setId(subtaskId);
        subtask.setName("Другое имя подзадачи");
        String subtaskJson = gson.toJson(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(subtasksUrl)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        Task updated = manager.getSubTaskById(subtaskId);
        assertNotNull(updated, "Задача не найдена");
        assertEquals("Другое имя подзадачи", updated.getName(), "Имя задачи не обновилось");

        List<Subtask> tasksFromManager = getSubtasks();
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
    }

    @Test
    public void testAddTask() throws IOException, InterruptedException {
        int epicId = manager.createEpic(createEpic());

        Subtask subtask = new Subtask("Test 2", "Testing subtask 2",
                Status.NEW, LocalDateTime.now(), Duration.ofMinutes(5), epicId);
        String taskJson = gson.toJson(subtask);

        HttpRequest request = HttpRequest.newBuilder().uri(subtasksUrl).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Subtask> tasksFromManager = manager.getSubTasks();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Test 2", tasksFromManager.get(0).getName(), "Некорректное имя задачи");
    }

    @Test
    public void testDeleteAllTask() throws IOException, InterruptedException {
        int epicId = manager.createEpic(createEpic());
        Subtask subtask1 = new Subtask("SubTask 1", "Desc 1", Status.NEW, LocalDateTime.now(), Duration.ofMinutes(10), epicId);
        Subtask subtask2 = new Subtask("SubTask 2", "Desc 2", Status.NEW, LocalDateTime.now().plusMinutes(15), Duration.ofMinutes(10), epicId);
        postSubtask(subtask1);
        postSubtask(subtask2);

        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .uri(subtasksUrl)
                .DELETE()
                .build();
        HttpResponse<String> deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, deleteResponse.statusCode(), "Удаление задач не прошло");

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(subtasksUrl)
                .GET()
                .build();
        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, getResponse.statusCode(), "Не удалось получить задачи после удаления");
        Task[] tasks = gson.fromJson(getResponse.body(), Task[].class);
        assertEquals(0, tasks.length, "Количество задач должно быть 0 после удаления");
    }

    @Test
    void testDeleteTaskById() throws IOException, InterruptedException {
        int epicId = manager.createEpic(createEpic());
        Subtask subtask1 = new Subtask("SubTask 1", "Desc 1", Status.NEW, LocalDateTime.now(), Duration.ofMinutes(10), epicId);
        int subtaskId = postSubtask(subtask1);

        URI url = URI.create("http://localhost:8080/subtasks/" + subtaskId);
        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();
        HttpResponse<String> deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, deleteResponse.statusCode(), "Удаление задач не прошло");

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(subtasksUrl)
                .GET()
                .build();
        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, getResponse.statusCode(), "Не удалось получить задачи после удаления");
        Subtask[] subtasks = gson.fromJson(getResponse.body(), Subtask[].class);
        assertEquals(0, subtasks.length, "Количество задач должно быть 0 после удаления");
    }

    private int postSubtask(Subtask subtask) throws IOException, InterruptedException {
        String subtaskJson = gson.toJson(subtask);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(subtasksUrl)
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
        HttpRequest request = HttpRequest.newBuilder()
                .uri(subtasksUrl)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "GET /subtasks должен возвращать 200");
        Subtask[] subtasksArray = gson.fromJson(response.body(), Subtask[].class);
        return Arrays.asList(subtasksArray);
    }

    protected Epic createEpic() {
        return new Epic("Epic1", "Epic Description");
    }
}