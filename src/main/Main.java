package main;

import manage.FileBackedTaskManager;
import task.Epic;
import task.Subtask;
import task.Task;
import task.Status;

public class Main {
    private static final FileBackedTaskManager taskManager = new FileBackedTaskManager();

    public static void main(String[] args) {
        addTasks();
        printAllTasks();
        printViewHistory();
    }

    private static void addTasks() {
        // Базовые задачи
        taskManager.addTask(new Task(0, "Пороги", "Переварить оба порога у хорошего мастера", Status.NEW));
        taskManager.addTask(new Task(0, "Где Перри?", "Найти Перри утконоса", Status.NEW));
        taskManager.addTask(new Task(0, "Сэнди", "Не дать Сэнди испортить лето Фербу и Фербу", Status.NEW));

        // Обновление задачи
        Task digestPorogy = taskManager.getTasks().get(0);
        taskManager.updateTask(new Task(digestPorogy.getId(), "Левый порог", "Переварить левый порог", Status.IN_PROGRESS));

        // Эпики
        Epic epicPaintCar = new Epic(0, "Покраска машины", "Покрасить разные элементы машины", Status.NEW);
        Epic epicFunny = new Epic(0, "Отправиться в отпуск", "По возможности захватить Польшу :D", Status.NEW);

        taskManager.addEpic(epicPaintCar);
        taskManager.addEpic(epicFunny);

        // Подзадачи для первого эпика
        Subtask[] subTasks1 = {
                new Subtask(0, "Крыло", "Покрасить крыло", Status.NEW, epicPaintCar.getId()),
                new Subtask(0, "Левый порог", "Покрасить левый порог", Status.NEW, epicPaintCar.getId())};

        for (Subtask subTask : subTasks1) {
            taskManager.addSubTask(subTask);
        }

        // Помечаем все подзадачи первого эпика как выполненные
        for (Subtask subTask : subTasks1) {
            subTask.setStatus(Status.DONE);
            taskManager.updateSubtask(subTask);
        }

        // Подзадачи для второго эпика
        taskManager.addSubTask(new Subtask(0, "Найти жинку", "Найти жинку и устроить свадьбу", Status.NEW, epicFunny.getId()));
        Subtask subTask22 = new Subtask(0, "Свадьба", "Захватить Польшу", Status.NEW, epicFunny.getId());
        taskManager.addSubTask(subTask22);
        subTask22.setStatus(Status.DONE);
        taskManager.updateSubtask(subTask22);
    }

    private static void printAllTasks() {
        System.out.println("\nЗадачи:");
        for (Task task : taskManager.getTasks()) {
            System.out.println(task);
        }

        System.out.println("\nЭпики:");
        for (Epic epic : taskManager.getEpics()) {
            System.out.println(epic);
            System.out.println("Подзадачи:");
            for (Task task : taskManager.getEpicSubtasks(epic)) {
                System.out.println("  - " + task);
            }
        }

        System.out.println("\nВсе подзадачи:");
        for (Task subtask : taskManager.getSubtasks()) {
            System.out.println(subtask);
        }
    }

    private static void printViewHistory() {
        System.out.println("\nФормируем историю просмотров:");

        // Формируем историю просмотров
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
        for (Task task : taskManager.getHistory()) {
            System.out.println(task);
        }
    }
}