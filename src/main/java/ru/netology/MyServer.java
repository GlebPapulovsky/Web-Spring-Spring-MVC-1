package ru.netology;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

public class MyServer {
    private final Map<String, Map<String, ru.netology.Handler>> handlers = new HashMap<>();

    public synchronized void addHandler(String method, String path, ru.netology.Handler handler) {
        handlers.putIfAbsent(method, new HashMap<>());
        handlers.get(method).put(path, handler);
    }

    public void listen(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClient(Socket clientSocket) {
        try (BufferedOutputStream responseStream = new BufferedOutputStream(clientSocket.getOutputStream());
             InputStream inputStream = clientSocket.getInputStream()) {
            ru.netology.Request request = (ru.netology.Request) parseRequest(inputStream);
            ru.netology.Handler handler = (ru.netology.Handler) findHandler(request);

            if (handler != null) {
                handler.handle(request, responseStream);
            } else {
                responseStream.write("HTTP/1.1 404 Not Found\r\n\r\n".getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ru.netology.Request parseRequest(InputStream inputStream) {
        StringBuilder requestLineBuilder = new StringBuilder();
        Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.name());

        // Читаем первую строку запроса
        if (scanner.hasNextLine()) {
            requestLineBuilder.append(scanner.nextLine());
        }

        // Разделение строки запроса на метод, путь и версию HTTP
        String requestLine = requestLineBuilder.toString();
        String[] requestParts = requestLine.split(" ", 3);
        String method = requestParts[0]; // HTTP метод
        String uri = requestParts[1]; // URI

        // Разделение URI на path и query
        String path;
        String query = null;
        int queryIndex = uri.indexOf('?');

        if (queryIndex >= 0) {
            path = uri.substring(0, queryIndex); // путь до знака вопроса
            query = uri.substring(queryIndex + 1); // часть после знака вопроса
        } else {
            path = uri; // если знака вопроса нет, весь URI - это путь
        }

        // Получение заголовков
        Map<String, String> headers = new HashMap<>();
        while (scanner.hasNextLine()) {
            String headerLine = scanner.nextLine();
            if (headerLine.isEmpty()) {
                break; // пустая строка обозначает конец заголовков
            }
            String[] headerParts = headerLine.split(": ", 2);
            if (headerParts.length == 2) {
                headers.put(headerParts[0], headerParts[1]);
            }
        }

        // Чтение тела запроса, если необходимо
        InputStream bodyInputStream = InputStream.nullInputStream(); // По умолчанию поток пустой
        if (headers.containsKey("Content-Length")) {
            int contentLength = Integer.parseInt(headers.get("Content-Length"));
            byte[] bodyBytes = new byte[contentLength];
            try {
                inputStream.read(bodyBytes, 0, contentLength);
                bodyInputStream = new ByteArrayInputStream(bodyBytes); // создаем поток с телом запроса
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Обработка параметров запроса
        Map<String, String> queryParams = query != null ? parseQueryParams(query) : new HashMap<>();

        // Возврат нового объекта Request с необходимыми полями
        return new ru.netology.Request(method, path, headers, bodyInputStream, queryParams);
    }

    private ru.netology.Handler findHandler(ru.netology.Request request) {
        Map<String, ru.netology.Handler> methodHandlers = handlers.get(request.getMethod());
        if (methodHandlers != null) {
            return methodHandlers.get(request.getPath());
        }
        return null;
    }

    private Map<String, String> parseQueryParams(String query) {
        List<NameValuePair> pairs = URLEncodedUtils.parse(query, StandardCharsets.UTF_8);
        return pairs.stream().collect(Collectors.toMap(NameValuePair::getName, NameValuePair::getValue));
    }
}