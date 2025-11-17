package api.handlers;

import api.BaseHttpHandler;
import com.sun.net.httpserver.HttpExchange;
import manager.task.TaskManager;
import task.Task;

import java.io.IOException;
import java.util.List;

public class PrioritizedTasksHandler extends BaseHttpHandler {

    public PrioritizedTasksHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    protected void processRequest(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        if (method.equals("GET") && path.equals("/prioritized")) {
            List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
            sendJson(exchange, prioritizedTasks, 200);
        } else {
            sendMethodNotAllowed(exchange);
        }
    }
}
