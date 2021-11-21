package com.score.handler;

import com.score.validator.UriValidator;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.util.logging.Logger;

public class ScoreBoardHttpHandler implements HttpHandler {
    final Logger LOGGER = Logger.getLogger(ScoreBoardHttpHandler.class.getName());

    @Override
    public void handle(HttpExchange httpExchange) {
        HttpHandler httpHandler = null;
        try {
            String requestedUri = httpExchange.getRequestURI().toString();
            if (UriValidator.isValidLoginUri(requestedUri)) {
                httpHandler = new LoginHandler();
            } else if (UriValidator.isValidUserLevelScoreUri(requestedUri)) {
                httpHandler = new UserScoreLevelHandler();
            } else if (UriValidator.isValidHighScoreUri(requestedUri)) {
                httpHandler = new HighScoreListHandler();
            }
            if (httpHandler != null) {
                httpHandler.handle(httpExchange);
            }
        } catch (Exception e) {
            LOGGER.warning(e.getMessage());
        }
    }
}
