package ru.netology;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Main {
  public static void main(String[] args) {
    MyServer server = new MyServer();

    // Добавляем обработчик для GET-запроса на корневой путь
    server.addHandler("GET", "/", new Handler() {
      @Override
      public void handle(Request request, BufferedOutputStream responseStream) {
        String responseBody = "Hello, World!";
        try {
          responseStream.write("HTTP/1.1 200 OK\r\n".getBytes(StandardCharsets.UTF_8));
          responseStream.write("Content-Length: ".getBytes(StandardCharsets.UTF_8));
          responseStream.write(String.valueOf(responseBody.length()).getBytes(StandardCharsets.UTF_8));
          responseStream.write("\r\n\r\n".getBytes(StandardCharsets.UTF_8));
          responseStream.write(responseBody.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });

    // Добавляем обработчик для POST-запроса на путь /submit

    server.addHandler("POST", "/submit", new Handler() {
      @Override
      public void handle(Request request, BufferedOutputStream responseStream) {
        try {
          String requestBody = new String(request.getBody().readAllBytes(), StandardCharsets.UTF_8);
          String responseBody = "Received: " + requestBody;
          responseStream.write("HTTP/1.1 200 OK\r\n".getBytes(StandardCharsets.UTF_8));
          responseStream.write("Content-Length: ".getBytes(StandardCharsets.UTF_8));
          responseStream.write(String.valueOf(responseBody.length()).getBytes(StandardCharsets.UTF_8));
          responseStream.write("\r\n\r\n".getBytes(StandardCharsets.UTF_8));
          responseStream.write(responseBody.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });

    // Запускаем сервер на порту 8080
    server.listen(8080);
  }
}