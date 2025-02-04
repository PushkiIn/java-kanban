package manager;

import enums.Status;
import model.*;

import java.util.HashMap;
import java.util.ArrayList;

public class TaskManager {
    private HashMap<Integer, Task> tasks;
    private HashMap<Integer, Epic> epics;
    private HashMap<Integer, SubTask> subTasks;
    private int counterId;

    public TaskManager() {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subTasks = new HashMap<>();
        counterId = 1;
    }

    private int generateId() {
        return ++counterId;
    }

    public void createTask(Task task) {
        task.setId(generateId());
        tasks.put(task.getId(), task);
    }

    public void createEpic(Epic epic) {
        epic.setId(generateId());
        epics.put(epic.getId(), epic);
    }

    public void createSubTask(SubTask subTask) {
        subTask.setId(generateId());
        subTasks.put(subTask.getId(), subTask);
        if(epics.get(subTask.getEpicId()) != null){
            (epics.get(subTask.getEpicId())).addSubTaskId(subTask.getId());
            updateEpicStatus((epics.get(subTask.getEpicId())));
        }
    }

    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public ArrayList<Epic> getAllEpics() {
        return  new ArrayList<>(epics.values());
    }

    public ArrayList<SubTask> getAllSubTasks() {
        return new ArrayList<>(subTasks.values());
    }

    public Task getTaskById(int id) {
        return tasks.get(id);
    }

    public void deleteAllTasks() {
        tasks.clear();
    }

    public void deleteAllEpics() {
        epics.clear();
        subTasks.clear();
    }

    public void deleteAllSubTasks() {
        if(!subTasks.isEmpty()){
            ArrayList<Integer> epicWithSubtasksIds = new ArrayList<>();
            for (SubTask subTask : subTasks.values()) {
                epics.get(subTask.getEpicId()).removeSubTaskId(subTask.getId());
                epicWithSubtasksIds.add(subTask.getEpicId());
            }
            subTasks.clear();
            for (int epicId : epicWithSubtasksIds) {
                updateEpicStatus(epics.get(epicId));
            }
        }
    }

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

    public void updateTask(Task updatedTask) {
        if (tasks.containsKey(updatedTask.getId())) {
            tasks.put(updatedTask.getId(), updatedTask);
        } else if (epics.containsKey(updatedTask.getId())) {
            if(updatedTask.getStatus() == epics.get(updatedTask.getId()).getStatus()){
                epics.put(updatedTask.getId(), (Epic) updatedTask);
            }
        } else if (subTasks.containsKey(updatedTask.getId())) {
            subTasks.put(updatedTask.getId(), (SubTask) updatedTask);
            updateEpicStatus(epics.get(((SubTask) updatedTask).getEpicId()));
        }
    }

    public ArrayList<SubTask> getSubTasksOfEpic(int epicId) {
        ArrayList<SubTask> subTasks = new ArrayList<>();

        for(int subTaskId : epics.get(epicId).getSubTaskIds()) {
            if(tasks.get(subTaskId) instanceof SubTask) {
                subTasks.add((SubTask) tasks.get(subTaskId));
            }
        }
        return subTasks;
    }

    private void updateEpicStatus(Epic epic) {
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
}
