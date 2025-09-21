package test;

import org.junit.jupiter.api.Test;
import task.Task;
import task.Status;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaskTest {

    @Test
    void testEqualityById() {
        Task task1 = new Task(1, "Тестовая задача", "Описание задачи", Status.NEW);
        Task task2 = new Task(1, "Другое название", "Иное описание", Status.IN_PROGRESS);
        assertTrue(task1.equals(task2), "Экземпляры классов Task равны по ID.");
    }

    @Test
    void testInheritanceEquality() {
        Task task1 = new Task(1, "Наследник", "Родительская задача", Status.NEW);
        Task task2 = new Task(1, "Другой потомок", "Ещё одна родительская задача", Status.IN_PROGRESS);
        assertTrue(task1.equals(task2), "Наследники класса Task равны по ID.");
    }
}