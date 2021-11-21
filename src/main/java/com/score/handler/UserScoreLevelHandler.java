package com.score.handler;

import com.score.model.UserScore;
import com.score.service.UserScoreService;
import com.score.session.SessionManager;
import com.score.session.UserSession;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_OK;

public class UserScoreLevelHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        int level = Integer.parseInt(httpExchange.getRequestURI().toString().split("/")[1]);
        var sessionKey = httpExchange.getRequestURI().toString().split("=")[1];

        var score = new BufferedReader(new InputStreamReader(httpExchange.getRequestBody(), StandardCharsets.US_ASCII)).lines().findFirst();

        if (score.isPresent() && isValidScore(score.get()) && SessionManager.getInstance().validSessionKey(sessionKey)) {
            var userSessionId = SessionManager.getInstance().getUserSessionByKey(sessionKey).map(UserSession::getId);
            if (userSessionId.isPresent()) {
                UserScoreService.getInstance().addScore(level, new UserScore(userSessionId.get(), Integer.parseInt(score.get())));
                httpExchange.sendResponseHeaders(HTTP_OK, 0);
            }
        } else {
            httpExchange.sendResponseHeaders(HTTP_BAD_REQUEST, -1);
        }
        httpExchange.close();

    }

    private boolean isValidScore(String s) {
        try {
            return Integer.parseInt(s) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
