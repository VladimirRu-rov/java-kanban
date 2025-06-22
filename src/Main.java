public class Main {

    public static void main(String[] args) {

        TaskManager taskManager = new TaskManager();

        Task digestPorogy = new Task("Переварить пороги", "У хорошего мастера");
        Task digestPorogyCreated = taskManager.addTask(digestPorogy);
        System.out.println(digestPorogyCreated);

        Task digestPorogyToUpdate = new Task(digestPorogy.getId(),
                "Обработать днище",
                "Хорошим средством",
                Status.IN_PROGRESS);
        Task digestPorogyUpdated = taskManager.updateTask(digestPorogyToUpdate);
        System.out.println(digestPorogyUpdated);

        Epic addSunroof = new Epic("Сделать люк в крыше", "Нужно успеть до следующего лета");
        taskManager.addEpic(addSunroof);
        System.out.println(addSunroof);

        Subtask addSunroofSubtask1 = new Subtask("Перекрасить машину", "Серый",
                addSunroof.getId());
        Subtask addSunroofSubtask2 = new Subtask("Воткнуть 2jz-gte", "Старый продать на Авито",
                addSunroof.getId());
        taskManager.addSubTask(addSunroofSubtask1);
        taskManager.addSubTask(addSunroofSubtask2);
        System.out.println(addSunroof);
        addSunroofSubtask2.setStatus(Status.DONE);
        taskManager.updateSubtask(addSunroofSubtask2);
        System.out.println(addSunroof);
    }
}
