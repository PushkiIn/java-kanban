import enums.Status;
import manager.TaskManager;
import model.*;

public class Main {
    public static void main(String[] args) {
        TaskManager taskManager = new TaskManager();

        Task task1 = new Task("Задача №1", "Что-то делаем");
        taskManager.createTask(task1);
        System.out.println("Создана задача: " + task1 + "\n");

        Task task2 = new Task("Задача №2", "Снова что то делаем", Status.IN_PROGRESS);
        taskManager.createTask(task2);
        System.out.println("Создана задача: " + task2 + "\n");

        Task task3 = new Task("Задача №3", "Уже выполненная задача", Status.DONE);
        taskManager.createTask(task3);
        System.out.println("Создана задача: " + task3 + "\n");


        Epic epic1 = new Epic("Эпик №1", "Что-то надо сделать");
        taskManager.createEpic(epic1);
        System.out.println("Создан эпик: " + epic1);

        SubTask subTask1 = new SubTask("Подзадача №1", "Первая подзадача для первого эпика", epic1.getId());
        SubTask subTask2 = new SubTask("Подзадача №2", "Вторая подзадача для первого эпика", Status.DONE, epic1.getId());
        taskManager.createSubTask(subTask1);
        taskManager.createSubTask(subTask2);
        System.out.println("Созданы подзадачи:");
        System.out.println(subTask1);
        System.out.println(subTask2);
        System.out.println("Статус первого эпика после создания подзадач: " + epic1.getStatus() + "\n");


        Epic epic2 = new Epic("Эпик №2", "дела дела");
        taskManager.createEpic(epic2);
        System.out.println("Создан эпик №2: " + epic2);
        System.out.println("Статус второго эпика после создания: " + epic2.getStatus() + "\n");

        SubTask subTask3 = new SubTask("Подзадача для эпика №2", "Подзадача для второго эпика", Status.IN_PROGRESS, epic2.getId());
        taskManager.createTask(subTask3);
        System.out.println("Создана подзадача:");
        System.out.println(subTask3);
        System.out.println("Статус второго эпика после создания подзадачи: " + epic2.getStatus() + "\n");


        subTask1.setStatus(Status.DONE);
        taskManager.updateTask(subTask1);
        System.out.println("Статус эпика после обновления подзадачи 1: " + epic1.getStatus());

        subTask2.setStatus(Status.IN_PROGRESS);
        taskManager.updateTask(subTask2);
        System.out.println("Статус эпика после обновления  подзадачи 2: " + epic1.getStatus());

        taskManager.deleteTask(subTask2.getId());
        System.out.println("Удалили подзадачу 2, статус эпика: " + epic1.getStatus());

        taskManager.deleteAllTasks();
        System.out.println("После удаления всех задач:");
        System.out.println("Все задачи: " + taskManager.getAllTasks());

        taskManager.deleteAllSubTasks();
        System.out.println("После удаления всех подзадач:");
        System.out.println("Все подзадачи: " + taskManager.getAllSubTasks());

        taskManager.deleteAllEpics();
        System.out.println("После удаления всех эпиков':");
        System.out.println("Все эпики: " + taskManager.getAllEpics());
    }
}
