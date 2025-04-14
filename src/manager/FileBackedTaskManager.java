package manager;

import model.Epic;
import model.SubTask;
import model.Task;
import tools.StringConverter;

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
        int maxId = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();

            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;

                Task task = StringConverter.fromString(line);
                if (task.getId() > maxId) maxId = task.getId();

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

        manager.counterId = maxId + 1;

        return manager;
    }

    public void save() {
        String fileName = "tasks.csv";
        String firstString = "id,type,name,status,description,epic\n";

        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write(firstString);

            for (Task task : super.getAllTasks()) {
                writer.write(StringConverter.toString(task) + "\n");
            }

            for (Epic epic : super.getAllEpics()) {
                writer.write(StringConverter.toString(epic) + "\n");
            }

            for (SubTask subTask : super.getAllSubTasks()) {
                writer.write(StringConverter.toString(subTask) + "\n");
            }

        } catch (IOException e) {
            System.out.println("Ошибка при записи в файл");
        }
    }
}
