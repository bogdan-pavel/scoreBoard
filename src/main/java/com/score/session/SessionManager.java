package com.score.session;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
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

    private final ConcurrentHashMap<Integer, UserSession> userSessions;

    private SessionManager() {
        userSessions = new ConcurrentHashMap<>();
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(this::removeUserSession, 0, PERIOD, TimeUnit.MINUTES);
    }

    public UserSession createUserSession(int userId, Date creationDate) {
        var userSession = userSessions.get(userId);
        if (userSession != null) {
            userSession = replaceUserSession(userId, creationDate, userSession);
        } else {
            userSession = createNewUserSession(userId, creationDate);
        }
        return userSession;
    }

    public static SessionManager getInstance() {
        return instance;
    }

    public void removeUserSession() {
        userSessions.forEach((key, userSession) -> {
            if (userSession.getCreationDate().before(getSessionExpireTime())) {
                userSession = userSessions.remove(userSession.getId());
                LOGGER.info("User sessionKey expired.Remove userSession" + userSession);
            }
        });
    }

    public Optional<UserSession> getUserSessionByKey(String sessionKey) {
        return userSessions.values().stream().filter(userSession -> userSession.getSessionKey().equals(sessionKey)).findFirst();
    }

    public boolean validSessionKey(String sessionKey) {
        return userSessions.entrySet().stream().anyMatch(entry -> entry.getValue().getSessionKey().equals(sessionKey));
    }

    private UserSession createNewUserSession(int userId, Date creationDate) {
        UserSession userSession;
        userSession = new UserSession(userId, createSessionKey(), creationDate);
        userSessions.put(userId, userSession);
        return userSession;
    }

    private UserSession replaceUserSession(int userId, Date creationDate, UserSession userSession) {
        userSession.updateCreationDate(creationDate);
        return userSessions.replace(userId, userSession);
    }

    private String createSessionKey() {
        return UUID.randomUUID().toString().split("-")[0];
    }

    private Date getSessionExpireTime() {
        return Date.from(Instant.now().minus(SESSION_TIME_OUT, ChronoUnit.MINUTES));
    }
}
