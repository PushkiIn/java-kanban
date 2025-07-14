package manager;

import enums.Status;
import exception.InvalidTaskTimingException;
import exception.OrphanSubTaskException;
import exception.TaskTimeConflictException;
import model.Epic;
import model.Subtask;
import model.Task;
import util.Managers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    protected final HashMap<Integer, Task> tasks;
    protected final HashMap<Integer, Epic> epics;
    protected final HashMap<Integer, Subtask> subTasks;
    protected int counterId = 0;
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
    public int createTask(Task task) throws InvalidTaskTimingException {
        try {
            validateTaskTiming(task);
        } catch (TaskTimeConflictException e) {
            System.out.println("Ошибка при добавлении задачи: " + e.getMessage());
            return -1;
        }
        int id = generateId();
        task.setId(id);
        tasks.put(task.getId(), task);
        prioritizedTasks.add(task);

        return id;
    }

    @Override
    public int createEpic(Epic epic) {
        int id = generateId();

        epic.setId(id);
        epics.put(epic.getId(), epic);
        updateEpicTime(epic);
        updateEpicStatus(epic);

        return id;
    }

    @Override
    public int createSubTask(Subtask subTask) {
        try {
            validateTaskTiming(subTask);
        } catch (TaskTimeConflictException e) {
            System.out.println("Ошибка при добавлении задачи: " + e.getMessage());
            return -1;
        }

        if (!epics.containsKey(subTask.getEpicId())) {
            throw new OrphanSubTaskException(
                    "Подзадача должна быть связана с существующим Epic. Предоставленный epicId:" + subTask.getEpicId()
            );
        }

        int id = generateId();
        subTask.setId(id);
        subTasks.put(id, subTask);

        Epic epic = epics.get(subTask.getEpicId());
        epic.addSubTaskById(id);
        updateEpicStatus(epic);
        updateEpicTime(epic);
        prioritizedTasks.add(subTask);

        return id;
    }

    private void validateTaskTiming(Task task) throws InvalidTaskTimingException, TaskTimeConflictException {
        if (task.getDuration() == null || task.getStartTime() == null) {
            throw new InvalidTaskTimingException("Необходимо указать время начала и продолжительность.");
        }

        if (hasTimeConflict(task)) {
            throw new TaskTimeConflictException("Время задачи конфликтует с существующими задачами.");
        }
    }

    @Override
    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<Subtask> getSubTasks() {
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
    public Subtask getSubTaskById(int id) {
        historyManager.add(subTasks.get(id));
        return subTasks.get(id);
    }

    @Override
    public void deleteTasks() {
        tasks.values().stream().forEach(task -> historyManager.remove(task.getId()));
        tasks.clear();
    }

    @Override
    public void deleteEpics() {
        for (Epic epic : epics.values()) {
            historyManager.remove(epic.getId());
        }
        for (Subtask subTask : subTasks.values()) {
            historyManager.remove(subTask.getId());
        }
        tasks.clear();
        epics.clear();
        subTasks.clear();
    }

    @Override
    public void deleteSubtasks() {
        for (Subtask subTask : subTasks.values()) {
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
    public void deleteTaskById(int id) {
        historyManager.remove(id);
        tasks.remove(id);
    }

    @Override
    public void deleteEpicById(int id) {
        historyManager.remove(id);
        if (epics.containsKey(id)) {
            for (int subTask : epics.get(id).getSubTasks()) {
                subTasks.remove(subTask);
            }
            epics.remove(id);
        }
    }

    @Override
    public void deleteSubTaskById(int id) {
        historyManager.remove(id);
        if (subTasks.containsKey(id)) {
            int epicId = subTasks.get(id).getEpicId();
            subTasks.remove(id);
            epics.get(epicId).removeSubTaskById(id);
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
            Subtask oldSubtask = subTasks.get(updatedTask.getId());

            if (oldSubtask.getStartTime() != null) {
                prioritizedTasks.remove(oldSubtask);
            }

            subTasks.put(updatedTask.getId(), (Subtask) updatedTask);

            if (updatedTask.getStartTime() != null) {
                prioritizedTasks.add(updatedTask);
            }

            updateEpicStatus(epics.get(((Subtask) updatedTask).getEpicId()));
            updateEpicTime(epics.get(((Subtask) updatedTask).getEpicId()));
        }
    }

    @Override
    public List<Subtask> getEpicSubTasks(int epicId) {
        return subTasks.values()
                .stream()
                .filter(subTask -> subTask.getEpicId() == epicId)
                .collect(Collectors.toList());
    }

    @Override
    public void updateEpicStatus(Epic epic) {
        if (epic.getSubTasks().isEmpty()) {
            epic.setStatus(Status.NEW);
        } else {
            ArrayList<Subtask> subTasksFromEpic = new ArrayList<>();

            for (int subTaskId : epic.getSubTasks()) {
                subTasksFromEpic.add(subTasks.get(subTaskId));
            }
            boolean allNEW = true;
            boolean allDONE = true;


            for (Subtask subTask : subTasksFromEpic) {
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
        List<Subtask> subTasksOfEpic = getEpicSubTasks(epic.getId());

        Optional<LocalDateTime> minStart = subTasksOfEpic.stream().map(Subtask::getStartTime).filter(Objects::nonNull).min(LocalDateTime::compareTo);
        Optional<LocalDateTime> maxEnd = subTasksOfEpic.stream().map(Subtask::getStartTime).filter(Objects::nonNull).max(LocalDateTime::compareTo);

        if (minStart.isPresent() && maxEnd.isPresent()) {
            epic.setStartTime(minStart.get());
            epic.setDuration(Duration.between(minStart.get(), maxEnd.get()));
            epic.setEndTime(maxEnd.get());
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
            LocalDateTime existingEnd = existing.getEndTime();

            return !(newEnd.isEqual(existingStart) || newEnd.isBefore(existingStart) || newStart.isEqual(existingEnd) || newStart.isAfter(existingEnd));
        });
    }
}