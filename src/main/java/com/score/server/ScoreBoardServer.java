package com.score.server;

import com.score.filter.RequestFilter;
import com.score.handler.ScoreBoardHttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class ScoreBoardServer {
    private static final Logger LOGGER = Logger.getLogger(ScoreBoardServer.class.getName());

    public static final String HOSTNAME = "localhost";
    public static final int PARALLELISM = Runtime.getRuntime().availableProcessors() * 20;
    private static final Integer HTTP_PORT = 8081;

    private final HttpServer httpServer;

    public ScoreBoardServer() throws IOException {
        httpServer = HttpServer.create(new InetSocketAddress(HOSTNAME, HTTP_PORT), 0);

        httpServer.createContext("/", new ScoreBoardHttpHandler()).getFilters().add(new RequestFilter());

        httpServer.setExecutor(Executors.newWorkStealingPool(PARALLELISM));

        addShutDownHook();
    }

    public void start() {
        httpServer.start();
    }

    public void stop() {
        if (this.httpServer != null) {
            this.httpServer.stop(0);
        }
    }

    private void addShutDownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Server is shutting down...");
            httpServer.stop(0);
            LOGGER.info("Server stopped!");
        }));
    }
}
