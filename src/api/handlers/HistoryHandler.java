package api.handlers;

import api.BaseHttpHandler;
import com.sun.net.httpserver.HttpExchange;
import manager.history.HistoryManager;
import manager.task.TaskManager;
import task.Task;

import java.io.IOException;
import java.util.List;

public class HistoryHandler extends BaseHttpHandler {

    private final HistoryManager historyManager;

    public HistoryHandler(TaskManager taskManager, HistoryManager historyManager) {
        super(taskManager);
        this.historyManager = historyManager;
    }

    @Override
    protected void processRequest(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        if ("GET".equals(method) && "/history".equals(path)) {
            handleGet(exchange);
        } else {
            sendMethodNotAllowed(exchange);
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        List<Task> history = historyManager.getHistory();
        sendJson(exchange, history, 200);
    }
}
