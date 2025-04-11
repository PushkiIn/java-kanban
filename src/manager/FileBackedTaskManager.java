package manager;

import enums.Status;
import enums.TaskType;
import model.Epic;
import model.SubTask;
import model.Task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileBackedTaskManager extends InMemoryTaskManager implements TaskManager {

    @Override
    public void createTask(Task task) {
        super.createTask(task);
        save();
    }

    @Override
    public void createEpic(Epic epic) {
        super.createEpic(epic);
        save();
    }

    @Override
    public void createSubTask(SubTask subTask) {
        super.createSubTask(subTask);
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public void deleteAllSubTasks() {
        super.deleteAllSubTasks();
        save();
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public void deleteSubTask(int id) {
        super.deleteSubTask(id);
        save();
    }

    @Override
    public void updateTask(Task updatedTask) {
        super.updateTask(updatedTask);
        save();
    }

    @Override
    public void updateEpicStatus(Epic epic) {
        super.updateEpicStatus(epic);
        save();
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();

            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;

                Task task = fromString(line);

                int id = task.getId();
                switch (task.getType()) {
                    case TASK -> manager.tasks.put(id, task);
                    case EPIC -> manager.epics.put(id, (Epic) task);
                    case SUBTASK -> {
                        SubTask subTask = (SubTask) task;
                        manager.subTasks.put(id, subTask);
                        Epic epic = manager.epics.get(subTask.getEpicId());
                        if (epic != null) {
                            epic.addSubTaskId(id);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return manager;
    }

    public void save() {
        try (FileWriter writer = new FileWriter("tasks.csv")) {
            writer.write("id,type,name,status,description,epic\n");

            for (Task task : super.getAllTasks()) {
                writer.write(toString(task) + "\n");
            }

            for (Epic epic : super.getAllEpics()) {
                writer.write(toString(epic) + "\n");
            }

            for (SubTask subTask : super.getAllSubTasks()) {
                writer.write(toString(subTask) + "\n");
            }

        } catch (IOException e) {
            System.out.println("Ошибка при записи в файл");
        }
    }

    public static Task fromString(String value) {
        String[] fields = value.split(",");
        int id = Integer.parseInt(fields[0]);
        TaskType type = TaskType.valueOf(fields[1]);
        String name = fields[2];
        Status status = Status.valueOf(fields[3]);
        String description = fields[4];
        int epicId = fields.length > 5 ? Integer.parseInt(fields[5]) : 0;

        switch (type) {
            case TASK:
                return new Task(id, name, description, status);
            case EPIC:
                return new Epic(id, name, description, status);
            case SUBTASK:
                return new SubTask(id, name, description, status, epicId);
            default:
                throw new IllegalArgumentException("Unknown task type: " + type);
        }
    }

    private String toString(Task task) {
        if (task instanceof SubTask) {
            SubTask subTask = (SubTask) task;
            return String.format("%d,%s,%s,%s,%s,%d",
                    subTask.getId(),
                    subTask.getType(),
                    subTask.getName(),
                    subTask.getStatus(),
                    subTask.getDescription(),
                    subTask.getEpicId()
            );
        } else {
            return String.format("%d,%s,%s,%s,%s",
                    task.getId(),
                    task.getType(),
                    task.getName(),
                    task.getStatus(),
                    task.getDescription()
            );
        }
    }
}
