package api.handlers;

import api.BaseHttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.history.HistoryManager;
import manager.task.TaskManager;
import task.Task;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class HistoryHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;
    private final HistoryManager historyManager;

    public HistoryHandler(TaskManager taskManager, HistoryManager historyManager) {
        this.taskManager = taskManager;
        this.historyManager = historyManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();

            switch (method) {
                case "GET":
                    if (path.equals("/history")) {
                        List<Task> history = historyManager.getHistory();
                        sendJson(exchange, history, 200);
                    } else {
                        sendBadRequest(exchange, "Неверный URL");
                    }
                    break;

                case "DELETE":
                    if (path.equals("/history")) {
                        historyManager.removeAll();
                        sendJson(exchange, Map.of("status", "cleared"), 200);
                    } else {
                        sendBadRequest(exchange, "Неверный URL для DELETE");
                    }
                    break;

                default:
                    sendMethodNotAllowed(exchange);
            }
        } catch (IOException e) {
            sendInternalError(exchange, "IO ошибка: " + e.getMessage());
        } catch (Exception e) {
            sendInternalError(exchange, "Внутренняя ошибка: " + e.getMessage());
        } finally {
            exchange.close();
        }
    }
}
