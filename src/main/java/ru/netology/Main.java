package ru.netology;


import com.sun.net.httpserver.Request;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class Main {
  public static void main(String[] args) {
    final var server=new Server();
    // добавление хендлеров (обработчиков)
    server.addHandler("GET", "/messages", new Handler() {
      @Override
      public void publish(LogRecord record) {
      }
      @Override
      public void flush() {
      }
      @Override
      public void close() throws SecurityException {

      }

      public void handle(Request request, BufferedOutputStream responseStream) throws IOException {
        String responseBody = "Это GET-запрос на /messages";
        responseStream.write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
        responseStream.write(responseBody.getBytes());
      }
    });
    server.addHandler("POST", "/messages", new Handler() {
      @Override
      public void publish(LogRecord record) {
      }
      @Override
      public void flush() {
      }
      @Override
      public void close() throws SecurityException {

      }

      public void handle(Request request, BufferedOutputStream responseStream) throws IOException {
        // Код обработки POST-запроса
        String responseBody = "Это POST-запрос на /messages";
        responseStream.write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
        responseStream.write(responseBody.getBytes());
      }
    });

    server.listen(8080);

  }
}


