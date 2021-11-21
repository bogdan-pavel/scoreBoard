package com.score.session;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class SessionManager {
    private static final Logger LOGGER = Logger.getLogger(SessionManager.class.getName());

    private static final SessionManager instance = new SessionManager();

    public static final int PERIOD = 1;
    public static final int SESSION_TIME_OUT = 10;

    private final Map<Integer, UserSession> userSessions;

    private SessionManager() {
        userSessions = new ConcurrentHashMap<>();
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this::removeUserSession, 0, PERIOD, TimeUnit.MINUTES);
    }

    public UserSession createUserSession(int userId, Date creationDate) {
        var userSession = userSessions.get(userId);
        if (userSession != null) {
            userSession.updateCreationDate(creationDate);
            userSessions.replace(userId, userSession);
        } else {
            userSession = new UserSession(userId, createSessionKey(),creationDate);
            userSessions.put(userId, userSession);
        }
        return userSession;
    }

    public static SessionManager getInstance() {
        return instance;
    }

    private String createSessionKey() {
        return UUID.randomUUID().toString().split("-")[0];
    }

    public void removeUserSession() {
        userSessions.forEach((key, userSession) -> {
            if (userSession.getCreationDate().before(getSessionExpireTime())) {
                userSession = userSessions.remove(userSession.getId());
                LOGGER.info("User sessionKey expired.Remove userSession" + userSession);
            }
        });
    }

    private Date getSessionExpireTime() {
        return Date.from(Instant.now().minus(SESSION_TIME_OUT, ChronoUnit.MINUTES));
    }

    public Optional<UserSession> getUserSessionByKey(String sessionKey) {
        return userSessions.values().stream().filter(userSession -> userSession.getSessionKey().equals(sessionKey)).findFirst();
    }

    public boolean validSessionKey(String sessionKey) {
        return userSessions.entrySet().stream().anyMatch(entry -> entry.getValue().getSessionKey().equals(sessionKey));
    }
}
