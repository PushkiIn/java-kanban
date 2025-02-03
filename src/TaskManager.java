import java.util.HashMap;
import java.util.ArrayList;

public class TaskManager {
    HashMap<Integer, Task> tasks;
    private static int counterId;

    public TaskManager() {
        tasks = new HashMap<>();
        counterId = 1;
    }

    public void createTask(Task task) {
        task.setId(counterId);
        tasks.put(counterId, task);
        counterId++;
        if(task instanceof SubTask) {
            SubTask subTask = (SubTask) task;
            if(tasks.get(subTask.getEpicId()) != null){
                ((Epic)tasks.get(subTask.getEpicId())).addSubTaskId(subTask.getId());
                updateEpicStatus((Epic) tasks.get(subTask.getEpicId()));
            }
        }
    }

    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public Task getTaskById(int id) {
        return tasks.get(id);
    }

    public void deleteAllTasks() {
        tasks.clear();
    }

    public void deleteTask(int id) {
        if(tasks.containsKey(id)) {
            if (tasks.get(id) instanceof SubTask){
                int epicId = ((SubTask) tasks.get(id)).getEpicId();
                tasks.remove(id);
                ((Epic)tasks.get(epicId)).removeSubTaskId(id);
                updateEpicStatus((Epic) tasks.get(epicId));
            }
            tasks.remove(id);
        }
    }

    public void updateTask(Task updatedTask) {
        if (tasks.containsKey(updatedTask.getId())) {
            tasks.put(updatedTask.getId(), updatedTask);
        }
    }

    public ArrayList<SubTask> getSubTasksOfEpic(Epic epic) {
        ArrayList<SubTask> subTasks = new ArrayList<SubTask>();

        for(int subTaskId : epic.getSubtaskIds()) {
            if(tasks.get(subTaskId) instanceof SubTask) {
                subTasks.add((SubTask) tasks.get(subTaskId));
            }
        }
        return subTasks;
    }

    public void updateTaskStatus(Task task, Status newStatus) {
        if (task instanceof Epic) {
            Epic epic = (Epic) task;
            updateEpicStatus(epic);
        } else if (task != null) {
            if (task instanceof SubTask) {
                int epicId = ((SubTask) task).getEpicId();
                task.setStatus(newStatus);
                updateEpicStatus((Epic) tasks.get(epicId));
            } else {
                task.setStatus(newStatus);
            }
        }
    }

    public void updateEpicStatus(Epic epic) {
        if (epic.getSubTaskIds().isEmpty()) {
            epic.setStatus(Status.NEW);
        } else {
            ArrayList<SubTask> subTasksFromEpic = new ArrayList<>();

            for(int subTaskId : epic.getSubTaskIds()) {
                subTasksFromEpic.add((SubTask) tasks.get(subTaskId));
            }

            boolean allNEW = true;
            boolean allDONE = true;
            for (SubTask subTask : subTasksFromEpic) {
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
