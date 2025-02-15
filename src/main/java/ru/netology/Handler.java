package ru.netology;

import java.io.BufferedOutputStream;


public interface Handler {
    void handle(ru.netology.Request request, BufferedOutputStream responseStream);
}