package com.score.handler;

import com.score.service.UserScoreService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.net.HttpURLConnection;

public class HighScoreListHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        var level = Integer.parseInt(httpExchange.getRequestURI().toString().split("/")[1]);
        var highScoresResponse = UserScoreService.getInstance().getHighScoreList(level);
        httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, highScoresResponse.length());
        httpExchange.getResponseBody().write(highScoresResponse.getBytes());
        httpExchange.close();
    }
}
