package manager;

import model.Epic;
import model.Subtask;
import model.Task;
import tools.StringConverter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileBackedTaskManager extends InMemoryTaskManager implements TaskManager {


    @Override
    public int createTask(Task task) {
        int id = super.createTask(task);
        save();

        return id;
    }

    @Override
    public int createEpic(Epic epic) {
        int id = super.createEpic(epic);
        save();

        return id;
    }

    @Override
    public int createSubTask(Subtask subTask) {
        int id = super.createSubTask(subTask);
        save();

        return id;
    }

    @Override
    public void deleteTasks() {
        super.deleteTasks();
        save();
    }

    @Override
    public void deleteEpics() {
        super.deleteEpics();
        save();
    }

    @Override
    public void deleteSubtasks() {
        super.deleteSubtasks();
        save();
    }

    @Override
    public void deleteTaskById(int id) {
        super.deleteTaskById(id);
        save();
    }

    @Override
    public void deleteEpicById(int id) {
        super.deleteEpicById(id);
        save();
    }

    @Override
    public void deleteSubTaskById(int id) {
        super.deleteSubTaskById(id);
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
                switch (task.getTaskType()) {
                    case TASK -> manager.tasks.put(id, task);
                    case EPIC -> manager.epics.put(id, (Epic) task);
                    case SUBTASK -> {
                        Subtask subTask = (Subtask) task;
                        manager.subTasks.put(id, subTask);
                        Epic epic = manager.epics.get(subTask.getEpicId());
                        if (epic != null) {
                            epic.addSubTaskById(id);
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
        String firstString = "id,type,name,status,description,duration,startTime,epic\n";

        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write(firstString);

            for (Task task : super.getTasks()) {
                writer.write(StringConverter.toString(task) + "\n");
            }

            for (Epic epic : super.getEpics()) {
                writer.write(StringConverter.toString(epic) + "\n");
            }

            for (Subtask subTask : super.getSubTasks()) {
                writer.write(StringConverter.toString(subTask) + "\n");
            }

        } catch (IOException e) {
            System.out.println("Ошибка при записи в файл");
        }
    }
}