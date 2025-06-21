package manager;

import enums.Status;
import model.Epic;
import model.SubTask;
import model.Task;
import util.Managers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    protected final HashMap<Integer, Task> tasks;
    protected final HashMap<Integer, Epic> epics;
    protected final HashMap<Integer, SubTask> subTasks;
    protected int counterId = 1;
    private final HistoryManager historyManager;
    private final TreeSet<Task> prioritizedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime, Comparator.nullsLast(Comparator.naturalOrder())).thenComparing(Task::getId));

    public InMemoryTaskManager() {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subTasks = new HashMap<>();
        historyManager = Managers.getDefaultHistory();
    }

    private int generateId() {
        return ++counterId;
    }

    @Override
    public void createTask(Task task) {
        task.setId(generateId());
        tasks.put(task.getId(), task);
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
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
            updateEpicTime((epics.get(subTask.getEpicId())));
            if (subTask.getStartTime() != null) {
                prioritizedTasks.add(subTask);
            }
        }
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<SubTask> getAllSubTasks() {
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
        tasks.values().stream().forEach(task -> historyManager.remove(task.getId()));
        tasks.clear();
    }

    @Override
    public void deleteAllEpics() {
        for (Epic epic : epics.values()) {
            historyManager.remove(epic.getId());
        }
        for (SubTask subTask : subTasks.values()) {
            historyManager.remove(subTask.getId());
        }
        tasks.clear();
        epics.clear();
        subTasks.clear();
    }

    @Override
    public void deleteAllSubTasks() {
        for (SubTask subTask : subTasks.values()) {
            historyManager.remove(subTask.getId());
        }
        subTasks.clear();
        for (Epic epic : epics.values()) {
            epic.removeAllSubTasks();
            updateEpicStatus(epic);
            updateEpicTime(epic);
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
        if (hasTimeConflict(updatedTask)) {
            return;
        }

        if (tasks.containsKey(updatedTask.getId())) {
            Task oldTask = tasks.get(updatedTask.getId());

            if (oldTask.getStartTime() != null) {
                prioritizedTasks.remove(oldTask);
            }

            tasks.put(updatedTask.getId(), updatedTask);

            if (updatedTask.getStartTime() != null) {
                prioritizedTasks.add(updatedTask);
            }
        } else if (epics.containsKey(updatedTask.getId())) {
            epics.get(updatedTask.getId()).setName(updatedTask.getName());
            epics.get(updatedTask.getId()).setDescription(updatedTask.getDescription());
        } else if (subTasks.containsKey(updatedTask.getId())) {
            SubTask oldSubTask = subTasks.get(updatedTask.getId());

            if (oldSubTask.getStartTime() != null) {
                prioritizedTasks.remove(oldSubTask);
            }

            subTasks.put(updatedTask.getId(), (SubTask) updatedTask);

            if (updatedTask.getStartTime() != null) {
                prioritizedTasks.add(updatedTask);
            }

            updateEpicStatus(epics.get(((SubTask) updatedTask).getEpicId()));
            updateEpicTime(epics.get(((SubTask) updatedTask).getEpicId()));
        }
    }

    @Override
    public List<SubTask> getEpicSubtasks(int epicId) {
        List<SubTask> subTasksByEpic = new ArrayList<>();

        return subTasks.values().stream().filter(subTask -> subTask.getEpicId() == epicId).collect(Collectors.toList());
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
    public void updateEpicTime(Epic epic) {
        List<SubTask> subTasksOfEpic = getEpicSubtasks(epic.getId());

        Optional<LocalDateTime> minStart = subTasksOfEpic.stream().map(SubTask::getStartTime).filter(Objects::nonNull).min(LocalDateTime::compareTo);
        Optional<LocalDateTime> maxEnd = subTasksOfEpic.stream().map(SubTask::getStartTime).filter(Objects::nonNull).max(LocalDateTime::compareTo);

        if (minStart.isPresent() && maxEnd.isPresent()) {
            epic.setStartTime(minStart.get());
            epic.setDuration(Duration.between(minStart.get(), maxEnd.get()));
        } else {
            epic.setStartTime(null);
            epic.setDuration(Duration.ZERO);
        }
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    public boolean hasTimeConflict(Task newTask) {
        if (newTask instanceof Epic) {
            return false;
        }

        if (newTask.getStartTime() == null || newTask.getDuration() == null) {
            return false;
        }

        LocalDateTime newStart = newTask.getStartTime();
        LocalDateTime newEnd = newStart.plus(newTask.getDuration());

        return getPrioritizedTasks().stream().filter(existing -> existing.getId() != newTask.getId()).filter(existing -> existing.getStartTime() != null && existing.getDuration() != null).anyMatch(existing -> {
            LocalDateTime existingStart = existing.getStartTime();
            LocalDateTime existingEnd = existingStart.plus(existing.getDuration());

            return !(newEnd.isBefore(existingStart) || newStart.isAfter(existingEnd));
        });
    }
}