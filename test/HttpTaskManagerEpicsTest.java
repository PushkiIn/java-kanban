import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerEpicsTest {
    private final TaskManager manager = new InMemoryTaskManager();
    private final HttpTaskServer taskServer = new HttpTaskServer(manager);
    private final Gson gson = Managers.getGson();
    private final HttpClient client = HttpClient.newHttpClient();
    private final URI epicsUrl = URI.create("http://localhost:8080/epics");

    public HttpTaskManagerEpicsTest() throws IOException {
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
    public void testGetAllEpics() throws IOException, InterruptedException {
        Epic epic1 = new Epic("Epic 1", "Desc 1");
        Epic epic2 = new Epic("Epic 2", "Desc 2");
        manager.createEpic(epic1);
        manager.createEpic(epic2);

        HttpRequest request = HttpRequest.newBuilder().uri(epicsUrl).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Epic[] epics = gson.fromJson(response.body(), Epic[].class);
        assertEquals(2, epics.length, "Количество эпиков должно быть 2");
    }

    @Test
    public void testGetEpicById() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 1", "Desc 1");
        int id = postEpic(epic);
        epic = manager.getEpicById(id);
        URI url = URI.create("http://localhost:8080/epics/" + id);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        Epic returned = gson.fromJson(response.body(), Epic.class);
        assertEquals(epic, returned, "Задачи должны быть идентичными");
    }

    @Test
    public void testGetEpicSubTasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Task 1", "Desc 1");
        int epicId = postEpic(epic);
        Subtask subtask1 = new Subtask("SubTask 1", "Desc 1", Status.NEW, LocalDateTime.now(), Duration.ofMinutes(10), epicId);
        Subtask subtask2 = new Subtask("SUbTask 2", "Desc 2", Status.NEW, LocalDateTime.now().plusMinutes(15), Duration.ofMinutes(10), epicId);
        subtask1.setId(postSubtask(subtask1));
        subtask2.setId(postSubtask(subtask2));
        URI url = URI.create("http://localhost:8080/epics/" + epicId + "/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Type subtaskListType = new TypeToken<List<Subtask>>() {
        }.getType();
        List<Subtask> subtasks = gson.fromJson(response.body(), subtaskListType);

        assertEquals(2, subtasks.size(), "Количество подзадач должно быть 2");
        assertEquals(subtask1, subtasks.get(0));
        assertEquals(subtask2, subtasks.get(1));
    }


    @Test
    public void testUpdateTask() throws IOException, InterruptedException {
        Epic epic = new Epic("Test 2", "Testing task 2");
        int epicId = postEpic(epic);
        epic.setId(epicId);
        epic.setName("Другое имя эпика");
        String taskJson = gson.toJson(epic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(epicsUrl)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        Task updated = manager.getEpicById(epicId);
        assertNotNull(updated, "Эпик не найден");
        assertEquals("Другое имя эпика", updated.getName(), "Имя задачи не обновилось");

        List<Epic> epicsFromManager = getEpics();
        assertEquals(1, epicsFromManager.size(), "Некорректное количество задач");
    }

    @Test
    public void testDeleteAllTask() throws IOException, InterruptedException {
        Epic epic1 = new Epic("epic 1", "Desc 1");
        Epic epic2 = new Epic("epic 2", "Desc 2");
        postEpic(epic1);
        postEpic(epic2);

        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .uri(epicsUrl)
                .DELETE()
                .build();
        HttpResponse<String> deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, deleteResponse.statusCode(), "Удаление задач не прошло");

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(epicsUrl)
                .GET()
                .build();
        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, getResponse.statusCode(), "Не удалось получить задачи после удаления");
        Epic[] epics = gson.fromJson(getResponse.body(), Epic[].class);
        assertEquals(0, epics.length, "Количество задач должно быть 0 после удаления");
    }

    @Test
    void testDeleteTaskById() throws IOException, InterruptedException {
        Epic epic = new Epic("Task 1", "Desc 1");
        int epicId = postEpic(epic);

        URI url = URI.create("http://localhost:8080/epics/" + epicId);
        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();
        HttpResponse<String> deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, deleteResponse.statusCode(), "Удаление эпиков не прошло");

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(epicsUrl)
                .GET()
                .build();
        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, getResponse.statusCode(), "Не удалось получить задачи после удаления");
        Epic[] epics = gson.fromJson(getResponse.body(), Epic[].class);
        assertEquals(0, epics.length, "Количество задач должно быть 0 после удаления");
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

    private int postEpic(Epic epic) throws IOException, InterruptedException {
        String epicJson = gson.toJson(epic);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(epicsUrl)
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

    private List<Epic> getEpics() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(epicsUrl)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "GET /tasks должен возвращать 200");
        Epic[] epicsArray = gson.fromJson(response.body(), Epic[].class);
        return Arrays.asList(epicsArray);
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
}