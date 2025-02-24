package manager;

import enums.Status;
import model.*;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

public class InMemoryTaskManager implements interfaces.TaskManager {
    private HashMap<Integer, Task> tasks;
    private HashMap<Integer, Epic> epics;
    private HashMap<Integer, SubTask> subTasks;
    private int counterId;
    private List<Task> history;

    public InMemoryTaskManager() {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subTasks = new HashMap<>();
        history = new ArrayList<>();
        counterId = 1;
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
        if(epics.get(subTask.getEpicId()) != null && epics.containsKey(subTask.getEpicId())){
            subTask.setId(generateId());
            subTasks.put(subTask.getId(), subTask);
            (epics.get(subTask.getEpicId())).addSubTaskId(subTask.getId());
            updateEpicStatus((epics.get(subTask.getEpicId())));
        }
    }

    @Override
    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public ArrayList<Epic> getAllEpics() {
        return  new ArrayList<>(epics.values());
    }

    @Override
    public ArrayList<SubTask> getAllSubTasks() {
        return new ArrayList<>(subTasks.values());
    }

    @Override
    public Task getTaskById(int id) {
        if(history.size() == 10) {
            history.removeFirst();
        }
        history.add(tasks.get(id));
        return tasks.get(id);
    }

    @Override
    public Task getEpicsById(int id) {
        if(history.size() == 10) {
            history.removeFirst();
        }
        history.add(epics.get(id));
        return epics.get(id);
    }

    @Override
    public SubTask getSubTaskById(int id) {
        if(history.size() == 10) {
            history.removeFirst();
        }
        history.add(subTasks.get(id));
        return subTasks.get(id);
    }

    @Override
    public void deleteAllTasks() {
        tasks.clear();
    }

    @Override
    public void deleteAllEpics() {
        epics.clear();
        subTasks.clear();
    }

    @Override
    public void deleteAllSubTasks() {
        subTasks.clear();
        for(Epic epic : epics.values()) {
            epic.removeAllSubTasks();
            updateEpicStatus(epic);
        }
    }

    @Override
    public void deleteTask(int id) {
        if(tasks.containsKey(id)) {
            tasks.remove(id);
        } else if (epics.containsKey(id)) {
            ArrayList<Integer> subTasksIds = ((Epic) tasks.get(id)).getSubTaskIds();
            epics.remove(id);
            for(int subTask : subTasksIds) {
                subTasks.remove(subTask);
            }
        } else if (subTasks.containsKey(id)) {
            int epicId = (subTasks.get(id)).getEpicId();
            subTasks.remove(id);
            (epics.get(epicId)).removeSubTaskId(id);
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
        ArrayList<SubTask> subTasks = new ArrayList<>();

        for(int subTaskId : epics.get(epicId).getSubTaskIds()) {
            if(tasks.get(subTaskId) instanceof SubTask) {
                subTasks.add((SubTask) tasks.get(subTaskId));
            }
        }
        return subTasks;
    }

    @Override
    public void updateEpicStatus(Epic epic) {
        if (epic.getSubTaskIds().isEmpty()) {
            epic.setStatus(Status.NEW);
        } else {
            ArrayList<SubTask> subTasksFromEpic = new ArrayList<>();

            for(int subTaskId : epic.getSubTaskIds()) {
                subTasksFromEpic.add(subTasks.get(subTaskId));
            }
            boolean allNEW = true;
            boolean allDONE = true;


            for (SubTask subTask : subTasksFromEpic) {
                if(subTask.getStatus() == Status.IN_PROGRESS) {
                    epic.setStatus(Status.IN_PROGRESS);
                    return;
                }
                if(subTask.getStatus() != Status.NEW) {
                    allNEW = false;
                }
                if(subTask.getStatus() != Status.DONE){
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
        return history;
    }
}
