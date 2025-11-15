package api;

import api.handlers.*;
import com.sun.net.httpserver.HttpServer;
import manager.Managers;
import manager.history.HistoryManager;
import manager.task.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private final HttpServer server;
    private final TaskManager taskManager;
    private final HistoryManager historyManager;

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.taskManager = taskManager;
        this.historyManager = taskManager.getHistoryManager();
        this.server = HttpServer.create(new InetSocketAddress(PORT), 0);


        server.setExecutor(new ThreadPoolExecutor(
                1,
                10,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                r -> {
                    Thread t = new Thread(r);
                    t.setDaemon(true);
                    return t;
                }
        ));
        configureRoutes();
    }

    public HttpTaskServer() throws IOException {
        this(Managers.getDefault());
    }

    public static void main(String[] args) {
        HttpTaskServer server;
        try {
            server = new HttpTaskServer();
            server.start();
        } catch (IOException e) {
            System.err.println("Ошибка при запуске сервера: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        System.out.println("Сервер работает.");
    }

    private void configureRoutes() {
        server.createContext("/tasks", new TaskHandler(taskManager));
        server.createContext("/subtasks", new SubtaskHandler(taskManager));
        server.createContext("/epics", new EpicHandler(taskManager));
        server.createContext("/history", new HistoryHandler(taskManager, historyManager));
        server.createContext("/prioritized", new PrioritizedTasksHandler(taskManager));
    }

    public void start() {
        server.start();
        System.out.println("Сервер запущен на порту " + PORT);
    }

    public void stop() {
        System.out.println("Остановка сервера...");
        try {
            server.stop(5);
            System.out.println("Сервер остановлен");
        } catch (Exception e) {
            System.err.println("Ошибка при остановке сервера: " + e.getMessage());
        }
    }
}


