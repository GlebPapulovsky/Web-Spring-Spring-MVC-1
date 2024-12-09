package ru.netology;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.*;

class Server {
    private final List<String> validPaths = List.of(
            "/index.html", "/spring.svg", "/spring.png", "/resources.html",
            "/styles.css", "/app.js", "/links.html", "/forms.html",
            "/classic.html", "/events.html", "/events.js"
    );
    private final ExecutorService threadPool = Executors.newFixedThreadPool(64);



    public void start() {

        try (ServerSocket serverSocket = new ServerSocket(9999)) {
            while (true) {
                Socket socket = serverSocket.accept();
                threadPool.submit(() -> handler(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handler(Socket socket) {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream())
        ) {
            String requestLine = in.readLine();
            String[] parts = requestLine.split(" ");

            if (parts.length != 3) {
                return;
            }

            String path = parts[1];
            if (!validPaths.contains(path)) {
                sendResponse(out, "404 Not Found", "Content-Length: 0");
                return;
            }

            Path filePath = Path.of(".", "public", path);
            String mimeType = Files.probeContentType(filePath);

            if (path.equals("/classic.html")) {
                sendClassicFile(out, filePath, mimeType);
            } else {
                sendFile(out, filePath, mimeType);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendResponse(BufferedOutputStream out, String status, String headers) throws IOException {
        out.write(("HTTP/1.1 " + status + "\r\n" + headers + "\r\n\r\n").getBytes());
        out.flush();
    }

    private void sendClassicFile(BufferedOutputStream out, Path filePath, String mimeType) throws IOException {
        String template = Files.readString(filePath);
        byte[] content = template.replace("{time}", LocalDateTime.now().toString()).getBytes();
        sendResponse(out, "200 OK", "Content-Type: " + mimeType + "\r\nContent-Length: " + content.length);
        out.write(content);
        out.flush();
    }

    private void sendFile(BufferedOutputStream out, Path filePath, String mimeType) throws IOException {
        long length = Files.size(filePath);
        sendResponse(out, "200 OK", "Content-Type: " + mimeType + "\r\nContent-Length: " + length);
        Files.copy(filePath, out);
        out.flush();
    }
}