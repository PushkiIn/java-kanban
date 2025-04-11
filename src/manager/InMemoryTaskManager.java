package manager;

import enums.Status;
import model.Epic;
import model.SubTask;
import model.Task;
import util.Managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryTaskManager implements TaskManager {
    protected final HashMap<Integer, Task> tasks;
    protected final HashMap<Integer, Epic> epics;
    protected final HashMap<Integer, SubTask> subTasks;
    private int counterId;
    private final HistoryManager historyManager;

    public InMemoryTaskManager() {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subTasks = new HashMap<>();
        counterId = 0;
        historyManager = Managers.getDefaultHistory();
    }

   private int generateId() {
        return ++counterId;
    }

    @Override
    public void createTask(Task task) {
        task.setId(generateId());
        tasks.put(task.getId(), task);
    }

    @Override
    public void createEpic(Epic epic) {
        epic.setId(generateId());
        epics.put(epic.getId(), epic);
    }

    @Override
    public void createSubTask(SubTask subTask) {
        if (epics.containsKey(subTask.getEpicId())) {
            subTask.setId(generateId());
            subTasks.put(subTask.getId(), subTask);
            epics.get(subTask.getEpicId()).addSubTaskId(subTask.getId());
            updateEpicStatus((epics.get(subTask.getEpicId())));
        }
    }

    @Override
    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public ArrayList<SubTask> getAllSubTasks() {
        return new ArrayList<>(subTasks.values());
    }

    @Override
    public Task getTaskById(int id) {
        historyManager.add(tasks.get(id));
        return tasks.get(id);
    }

    @Override
    public Epic getEpicById(int id) {
        historyManager.add(epics.get(id));
        return epics.get(id);
    }

    @Override
    public SubTask getSubTaskById(int id) {
        historyManager.add(subTasks.get(id));
        return subTasks.get(id);
    }

    @Override
    public void deleteAllTasks() {
        for (Task task: tasks.values()) {
            historyManager.remove(task.getId());
        }
        tasks.clear();
    }

    @Override
    public void deleteAllEpics() {
        for (Epic epic: epics.values()) {
            historyManager.remove(epic.getId());
        }
        for (SubTask subTask: subTasks.values()) {
            historyManager.remove(subTask.getId());
        }
        tasks.clear();
        epics.clear();
        subTasks.clear();
    }

    @Override
    public void deleteAllSubTasks() {
        for (SubTask subTask: subTasks.values()) {
            historyManager.remove(subTask.getId());
        }
        subTasks.clear();
        for (Epic epic : epics.values()) {
            epic.removeAllSubTasks();
            updateEpicStatus(epic);
        }
    }

    @Override
    public void deleteTask(int id) {
        historyManager.remove(id);
        tasks.remove(id);
    }

    @Override
    public void deleteEpic(int id) {
        historyManager.remove(id);
        if (epics.containsKey(id)) {
            for (int subTask : epics.get(id).getSubTaskIds()) {
                subTasks.remove(subTask);
            }
            epics.remove(id);
        }
    }

    @Override
    public void deleteSubTask(int id) {
        historyManager.remove(id);
        if (subTasks.containsKey(id)) {
            int epicId = subTasks.get(id).getEpicId();
            subTasks.remove(id);
            epics.get(epicId).removeSubTaskId(id);
            updateEpicStatus(epics.get(epicId));
        }
    }

    @Override
    public void updateTask(Task updatedTask) {
        if (tasks.containsKey(updatedTask.getId())) {
            tasks.put(updatedTask.getId(), updatedTask);
        } else if (epics.containsKey(updatedTask.getId())) {
            epics.get(updatedTask.getId()).setName(updatedTask.getName());
            epics.get(updatedTask.getId()).setDescription(updatedTask.getDescription());
        } else if (subTasks.containsKey(updatedTask.getId())) {
            subTasks.put(updatedTask.getId(), (SubTask) updatedTask);
            updateEpicStatus(epics.get(((SubTask) updatedTask).getEpicId()));
        }
    }

    @Override
    public ArrayList<SubTask> getSubTasksOfEpic(int epicId) {
        ArrayList<SubTask> subTasksByEpic = new ArrayList<>();
        for (SubTask subTask : subTasks.values()) {
            if (subTask.getEpicId() == epicId) {
                subTasksByEpic.add(subTask);
            }
        }
        return subTasksByEpic;
    }

    @Override
    public void updateEpicStatus(Epic epic) {
        if (epic.getSubTaskIds().isEmpty()) {
            epic.setStatus(Status.NEW);
        } else {
            ArrayList<SubTask> subTasksFromEpic = new ArrayList<>();

            for (int subTaskId : epic.getSubTaskIds()) {
                subTasksFromEpic.add(subTasks.get(subTaskId));
            }
            boolean allNEW = true;
            boolean allDONE = true;


            for (SubTask subTask : subTasksFromEpic) {
                if (subTask.getStatus() == Status.IN_PROGRESS) {
                    epic.setStatus(Status.IN_PROGRESS);
                    return;
                }
                if (subTask.getStatus() != Status.NEW) {
                    allNEW = false;
                }
                if (subTask.getStatus() != Status.DONE) {
                    allDONE = false;
                }
            }

            if (allNEW) {
                epic.setStatus(Status.NEW);
            } else if (allDONE) {
                epic.setStatus(Status.DONE);
            } else {
                epic.setStatus(Status.IN_PROGRESS);
            }
        }
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }
}