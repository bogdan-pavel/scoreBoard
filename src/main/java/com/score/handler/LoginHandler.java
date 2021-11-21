package com.score.handler;

import com.score.session.SessionManager;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Logger;

import static java.net.HttpURLConnection.HTTP_OK;

public class LoginHandler implements HttpHandler {
    private static final Logger LOGGER = Logger.getLogger(LoginHandler.class.getName());

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        int userId = Integer.parseInt(httpExchange.getRequestURI().toString().split("/")[1]);
        LOGGER.info("login service call userId = " + userId);
        var userSession = SessionManager.getInstance().createUserSession(userId,new Date());
        var sessionKey = userSession.getSessionKey();

        httpExchange.sendResponseHeaders(HTTP_OK, sessionKey.length());
        httpExchange.getResponseBody().write(sessionKey.getBytes());
        httpExchange.close();

        LOGGER.info("login service call userId: " + userId + ",sessionKey:" + sessionKey);
    }
}
