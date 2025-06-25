package main;

import manage.TaskManager;
import task.Epic;
import task.Subtask;
import task.Task;
import task.Status;

public class Main {

    public static void main(String[] args) {

        TaskManager taskManager = new TaskManager();

        Task digestPorogy = new Task("Пороги", "Переварить оба порога у хорошего мастера");
        Task createdTask1 = taskManager.addTask(digestPorogy);
        System.out.println(createdTask1);

        Task wherePerry = new Task("Где Перри?", "Найти Перри утконоса");
        Task createdTask2 = taskManager.addTask(wherePerry);
        System.out.println(createdTask2);

        Task Sandy = new Task("Сэнди", "Не дать Сэнди испортить лето Финесу и Фербу");
        Task createdTask3 = taskManager.addTask(Sandy);
        System.out.println(createdTask3);

        // Проверяем изменение статуса задачи
        Task updatedDigestPorogy = new Task(createdTask1.getId(), "Левый порог", "Переварить левый порог", Status.IN_PROGRESS);
        Task updatedResult1 = taskManager.updateTask(updatedDigestPorogy);
        System.out.println(updatedResult1);

        // Создаём эпики
        Epic epicPaintCar = new Epic("Покраска машины", "Покрасить разные элементы машины");
        Epic addedEpic1 = taskManager.addEpic(epicPaintCar);
        System.out.println(addedEpic1);

        Epic epicFunny = new Epic("Отправиться в отпуск", "По возможности захватить Польшу :D");
        Epic addedEpic2 = taskManager.addEpic(epicFunny);
        System.out.println(addedEpic2);

        // Добавляем подзадачи в первый эпик
        Subtask subtask1_1 = new Subtask("Крыло", "Покрасить крыло", addedEpic1.getId());
        Subtask subtask1_2 = new Subtask("Левый порог", "Покрасить левый порог", addedEpic1.getId());
        Subtask subtask1_3 = new Subtask("Правый порог", "Покрасить правый порог", addedEpic1.getId());
        Subtask subtask1_4 = new Subtask("Крыша", "Купить новую крышу с люком", addedEpic1.getId());
        Subtask subtask1_5 = new Subtask("Бампер", "Покрасить бампер", addedEpic1.getId());

        taskManager.addSubTask(subtask1_1);
        taskManager.addSubTask(subtask1_2);
        taskManager.addSubTask(subtask1_3);
        taskManager.addSubTask(subtask1_4);
        taskManager.addSubTask(subtask1_5);

        // Просматриваем состояние первого эпика после добавления подзадач
        System.out.println(addedEpic1);

        // Изменяем статус первой подзадачи на "Выполнено"
        subtask1_1.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask1_1);
        System.out.println(addedEpic1);

        // Завершаем вторую подзадачу
        subtask1_2.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask1_2);
        System.out.println(addedEpic1);

        // Завершена третья подзадача
        subtask1_3.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask1_3);
        System.out.println(addedEpic1);

        // Выполняем четвертую подзадачу
        subtask1_4.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask1_4);
        System.out.println(addedEpic1);

        // Пятая подзадача ещё не сделана
        System.out.println("Последняя подзадача: " + subtask1_5);

        // А теперь выполняем пятую подзадачу
        subtask1_5.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask1_5);
        System.out.println(addedEpic1); // Все подзадачи выполнены, значит весь эпик тоже считается завершённым

        // Рассмотрим второй эпик
        Subtask subtask2_1 = new Subtask("Найти жинку", "Найти жинку и устроить свадьбу", addedEpic2.getId());
        Subtask subtask2_2 = new Subtask("Свадьба", "Захватить Польшу", addedEpic2.getId());
        taskManager.addSubTask(subtask2_1);
        taskManager.addSubTask(subtask2_2);

        // Вторая подзадача выполнена сразу
        subtask2_2.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask2_2);
        System.out.println(addedEpic2);

        // Попробуем удалить одну из задач
        taskManager.deleteTaskByID(createdTask1.getId());
        System.out.println("\nУдалены задачи:");
        System.out.println(taskManager.getTasks());

        // Удалим один из эпиков
        taskManager.deleteEpicByID(addedEpic2.getId());
        System.out.println("\nОстался единственный эпик:");
        System.out.println(taskManager.getEpics());

        System.out.println("\nСписок оставшихся задач:");
        System.out.println(taskManager.getTasks());

        System.out.println("\nСписок оставшихся эпиков:");
        System.out.println(taskManager.getEpics());

        System.out.println("\nСписок подзадач последнего эпика:");
        System.out.println(taskManager.getEpicSubtasks(addedEpic1));
    }
}
