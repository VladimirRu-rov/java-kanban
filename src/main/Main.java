package main;

import manage.FileBackedTaskManager;
import task.Epic;
import task.Subtask;
import task.Task;
import task.Status;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;


public class Main {
    private static final FileBackedTaskManager taskManager = new FileBackedTaskManager();

    public static void main(String[] args) {
        addTasks();
        printAllTasks();
        printViewHistory();
    }

    private static void addTasks() {
        taskManager.addTask(new Task(1, "Пороги", "Переварить оба порога у хорошего мастера", Status.NEW, Duration.ofMinutes(180), LocalDateTime.now().plusDays(2)));
        taskManager.addTask(new Task(2, "Где Перри?", "Найти Перри утконоса", Status.NEW, Duration.ofMinutes(60), LocalDateTime.now().plusDays(1)));
        taskManager.addTask(new Task(3, "Сэнди", "Не дать Сэнди испортить лето Фербу и Фербу", Status.NEW, Duration.ofMinutes(120), LocalDateTime.now().plusDays(3)));

        Task digestPorogy = taskManager.getTasks().get(0);
        taskManager.updateTask(new Task(digestPorogy.getId(), "Левый порог", "Переварить левый порог", Status.IN_PROGRESS, digestPorogy.getDuration(), digestPorogy.getStartTime()));


        Epic epicPaintCar = new Epic(10, "Покраска машины", "Покрасить разные элементы машины", Status.NEW, Duration.ofMinutes(0), LocalDateTime.now());
        Epic epicFunny = new Epic(11, "Отправиться в отпуск", "По возможности захватить Польшу :D", Status.NEW, Duration.ofMinutes(0), LocalDateTime.now());
        taskManager.addEpic(epicPaintCar);
        taskManager.addEpic(epicFunny);

        List<Subtask> subTasks1 = List.of(
                new Subtask(20, "Крыло", "Покрасить крыло", Status.NEW, Duration.ofMinutes(90), LocalDateTime.now().plusDays(4), epicPaintCar.getId()),
                new Subtask(21, "Левый порог", "Покрасить левый порог", Status.NEW, Duration.ofMinutes(60), LocalDateTime.now().plusDays(5), epicPaintCar.getId())
        );

        subTasks1.forEach(taskManager::addSubTask);

        subTasks1.forEach(subTask -> {
            subTask.setStatus(Status.DONE);
            taskManager.updateSubtask(subTask);
        });

        taskManager.addSubTask(new Subtask(30, "Найти жинку", "Найти жинку и устроить свадьбу", Status.NEW, Duration.ofMinutes(1440), LocalDateTime.now().plusDays(10), epicFunny.getId()));
        Subtask subTask22 = new Subtask(31, "Свадьба", "Захватить Польшу", Status.NEW, Duration.ofMinutes(720), LocalDateTime.now().plusDays(11), epicFunny.getId());
        taskManager.addSubTask(subTask22);
        subTask22.setStatus(Status.DONE);
        taskManager.updateSubtask(subTask22);
    }

    private static void printAllTasks() {
        System.out.println("\nЗадачи:");
        taskManager.getTasks().forEach(System.out::println);


        System.out.println("\nЭпики:");
        taskManager.getEpics().forEach(epic -> {
            System.out.println(epic);
            System.out.println("Подзадачи:");
            taskManager.getEpicSubtasks(epic).forEach(subtask -> System.out.println("  - " + subtask));
        });

        System.out.println("\nВсе подзадачи:");
        taskManager.getSubtasks().forEach(System.out::println);
    }

    private static void printViewHistory() {
        System.out.println("\nФормируем историю просмотров:");

        taskManager.getTaskByID(2);
        taskManager.getTaskByID(1);
        taskManager.getTaskByID(3);
        taskManager.getEpicByID(5);
        taskManager.getEpicByID(4);
        taskManager.getTaskByID(1);
        taskManager.getSubtaskByID(7);
        taskManager.getSubtaskByID(6);
        taskManager.getSubtaskByID(9);
        taskManager.getSubtaskByID(10);
        taskManager.getTaskByID(1);

        System.out.println("\nИстория просмотров:");
        taskManager.getHistory().forEach(System.out::println); // Замена цикла
    }
}