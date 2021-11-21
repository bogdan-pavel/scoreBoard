package com.score;

import com.score.server.ScoreBoardServer;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Application {
    private static final Logger LOGGER = Logger.getLogger("confLogger");

    public static void main(String[] args) {
        try {
            var server = new ScoreBoardServer();
            server.start();
            LOGGER.info("\nScoreboard server started!");

            System.out.println("\nPress Enter to stop the server. ");
            System.in.read();

            server.stop();

            LOGGER.warning("\nScoreboard server stopped!");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Scoreboard server terminated unexpectedly!", e);
        }

    }
}
