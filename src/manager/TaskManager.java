package manager;

import model.Epic;
import model.Subtask;
import model.Task;

import java.util.List;

public interface TaskManager {
    int createTask(Task task);

    int createEpic(Epic epic);

    int createSubTask(Subtask subTask);

    List<Task> getTasks();

    List<Epic> getEpics();

    List<Subtask> getSubTasks();

    Task getTaskById(int id);

    Epic getEpicById(int id);

    Subtask getSubTaskById(int id);

    void deleteTasks();

    void deleteEpics();

    void deleteSubtasks();

    void deleteTaskById(int id);

    void deleteEpicById(int id);

    void deleteSubTaskById(int id);

    void updateTask(Task updatedTask);

    List<Subtask> getEpicSubTasks(int epicId);

    void updateEpicStatus(Epic epic);

    List<Task> getHistory();

    void updateEpicTime(Epic epic);

    List<Task> getPrioritizedTasks();
}