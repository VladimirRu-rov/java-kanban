package main;

import manage.TaskManager;
import task.Epic;
import task.Subtask;
import task.Task;
import task.Status;
import manage.InMemoryTaskManager;
import manage.Managers;

public class Main {
    private static final TaskManager taskManager = Managers.getDefault();

    public static void main(String[] args) {
        addTasks();
        printAllTasks();
        printViewHistory();
    }

    private static void addTasks() {
        Task digestPorogy = new Task("Пороги", "Переварить оба порога у хорошего мастера");
        taskManager.addTask(digestPorogy);

        Task wherePerry = new Task("Где Перри?", "Найти Перри утконоса");
        taskManager.addTask(wherePerry);

        Task sandy = new Task("Сэнди", "Не дать Сэнди испортить лето Финесу и Фербу");
        taskManager.addTask(sandy);

        // Проверяем изменение статуса задачи
        Task updatedDigestPorogy = new Task(digestPorogy.getId(), "Левый порог", "Переварить левый порог", Status.IN_PROGRESS);
        taskManager.updateTask(updatedDigestPorogy);

        // Эпики
        Epic epicPaintCar = new Epic("Покраска машины", "Покрасить разные элементы машины");
        taskManager.addEpic(epicPaintCar);

        Epic epicFunny = new Epic("Отправиться в отпуск", "По возможности захватить Польшу :D");
        taskManager.addEpic(epicFunny);

        // Добавляем подзадачи в первый эпик
        Subtask subtask1_1 = new Subtask("Крыло", "Покрасить крыло", epicPaintCar.getId());
        Subtask subtask1_2 = new Subtask("Левый порог", "Покрасить левый порог", epicPaintCar.getId());
        Subtask subtask1_3 = new Subtask("Правый порог", "Покрасить правый порог", epicPaintCar.getId());
        Subtask subtask1_4 = new Subtask("Крыша", "Купить новую крышу с люком", epicPaintCar.getId());
        Subtask subtask1_5 = new Subtask("Бампер", "Покрасить бампер", epicPaintCar.getId());

        taskManager.addSubTask(subtask1_1);
        taskManager.addSubTask(subtask1_2);
        taskManager.addSubTask(subtask1_3);
        taskManager.addSubTask(subtask1_4);
        taskManager.addSubTask(subtask1_5);

        // Просматриваем состояние первого эпика после добавления подзадач
        System.out.println(epicPaintCar);

        // Изменяем статус первой подзадачи на "Выполнено"
        subtask1_1.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask1_1);
        System.out.println(epicPaintCar);

        // Завершаем вторую подзадачу
        subtask1_2.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask1_2);
        System.out.println(epicPaintCar);

        // Завершена третья подзадача
        subtask1_3.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask1_3);
        System.out.println(epicPaintCar);

        // Выполняем четвертую подзадачу
        subtask1_4.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask1_4);
        System.out.println(epicPaintCar);

        // Пятая подзадача ещё не сделана
        System.out.println("Последняя подзадача: " + subtask1_5);

        // А теперь выполняем пятую подзадачу
        subtask1_5.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask1_5);
        System.out.println(epicPaintCar); // Теперь эпик выполнен целиком

        // Рассмотрим второй эпик
        Subtask subtask2_1 = new Subtask("Найти жинку", "Найти жинку и устроить свадьбу", epicFunny.getId());
        Subtask subtask2_2 = new Subtask("Свадьба", "Захватить Польшу", epicFunny.getId());
        taskManager.addSubTask(subtask2_1);
        taskManager.addSubTask(subtask2_2);

        // Вторая подзадача выполнена сразу
        subtask2_2.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask2_2);
        System.out.println(epicFunny);
    }

    private static void printAllTasks() {
        System.out.println("Задачи:");
        for (Task task : taskManager.getTasks()) {
            System.out.println(task);
            System.out.println();
        }

        System.out.println("Эпики:");
        for (Epic epic : taskManager.getEpics()) {
            System.out.println(epic);
            System.out.println();

            for (Task task : taskManager.getEpicSubtasks(epic)) {
                System.out.println("--> " + task);
            }
        }

        System.out.println("Подзадачи:");
        for (Task subtask : taskManager.getSubtasks()) {
            System.out.println(subtask);
            System.out.println();
        }
    }

    private static void printViewHistory() {
        // Просматриваем 11 задач, в истории должны отобразиться последние 10
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

        System.out.println();
        System.out.println("История просмотров:");
        for (Task task : taskManager.getHistory()) {
            System.out.println(task);
        }
    }
}


