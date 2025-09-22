package main;

import manage.TaskManager;
import task.Epic;
import task.Subtask;
import task.Task;
import task.Status;
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
        Subtask subTask11 = new Subtask("Крыло", "Покрасить крыло", epicPaintCar.getId());
        Subtask subTask12 = new Subtask("Левый порог", "Покрасить левый порог", epicPaintCar.getId());
        Subtask subTask13 = new Subtask("Правый порог", "Покрасить правый порог", epicPaintCar.getId());
        Subtask subTask14 = new Subtask("Крыша", "Купить новую крышу с люком", epicPaintCar.getId());
        Subtask subTask15 = new Subtask("Бампер", "Покрасить бампер", epicPaintCar.getId());

        taskManager.addSubTask(subTask11);
        taskManager.addSubTask(subTask12);
        taskManager.addSubTask(subTask13);
        taskManager.addSubTask(subTask14);
        taskManager.addSubTask(subTask15);
      
        // Просматриваем состояние первого эпика после добавления подзадач
        System.out.println(epicPaintCar);

        // Изменяем статус первой подзадачи на "Выполнено"
        subTask11.setStatus(Status.DONE);
        taskManager.updateSubtask(subTask11);
        System.out.println(epicPaintCar);

        // Завершаем вторую подзадачу
        subTask12.setStatus(Status.DONE);
        taskManager.updateSubtask(subTask12);
        System.out.println(epicPaintCar);

        // Завершена третья подзадача
        subTask13.setStatus(Status.DONE);
        taskManager.updateSubtask(subTask13);
        System.out.println(epicPaintCar);

        // Выполняем четвертую подзадачу
        subTask14.setStatus(Status.DONE);
        taskManager.updateSubtask(subTask14);
        System.out.println(epicPaintCar);

        // Пятая подзадача ещё не сделана
        System.out.println("Последняя подзадача: " + subTask15);

        // А теперь выполняем пятую подзадачу
        subTask15.setStatus(Status.DONE);
        taskManager.updateSubtask(subTask15);
        System.out.println(epicPaintCar); // Теперь эпик выполнен целиком

        // Рассмотрим второй эпик
        Subtask subTask21 = new Subtask("Найти жинку", "Найти жинку и устроить свадьбу", epicFunny.getId());
        Subtask subTask22 = new Subtask("Свадьба", "Захватить Польшу", epicFunny.getId());
        taskManager.addSubTask(subTask21);
        taskManager.addSubTask(subTask22);

        // Вторая подзадача выполнена сразу
        subTask22.setStatus(Status.DONE);
        taskManager.updateSubtask(subTask22);
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
