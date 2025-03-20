package manager;

import model.Epic;
import model.SubTask;
import model.Task;

import java.util.ArrayList;
import java.util.List;

public interface TaskManager {
    void createTask(Task task);

    void createEpic(Epic epic);

    void createSubTask(SubTask subTask);

    ArrayList<Task> getAllTasks();

    ArrayList<Epic> getAllEpics();

    ArrayList<SubTask> getAllSubTasks();

    Task getTaskById(int id);

    Epic getEpicById(int id);

    SubTask getSubTaskById(int id);

    void deleteAllTasks();

    void deleteAllEpics();

    void deleteAllSubTasks();

    void deleteTask(int id);

    void deleteEpic(int id);

    void deleteSubTask(int id);

    void updateTask(Task updatedTask);

    ArrayList<SubTask> getSubTasksOfEpic(int epicId);

    void updateEpicStatus(Epic epic);

    List<Task> getHistory();
}