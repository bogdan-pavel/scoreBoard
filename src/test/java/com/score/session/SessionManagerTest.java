package com.score.session;


import org.junit.Before;
import org.junit.Test;
import org.llorllale.cactoos.matchers.Assertion;
import org.llorllale.cactoos.matchers.RunsInThreads;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class SessionManagerTest {

    private SessionManager sessionManager;

    @Before
    public void setUp() {
        sessionManager = SessionManager.getInstance();
    }

    @Test
    public void givenNoSessionManagerInstance_whenGetInstanceMultipleTimes_thenReturnSameInstance() {
        assertSame(SessionManager.getInstance(), SessionManager.getInstance());
    }

    @Test
    public void givenNoUserSession_whenCreateUserSession_thenUserSessionCreated() {
        UserSession userSession = sessionManager.createUserSession(1, new Date());

        assertNotNull(userSession);
        assertEquals(userSession.getId(), 1);

        assertNotNull(userSession.getSessionKey());
        assertFalse(userSession.getSessionKey().isEmpty());

        Date creationDate = userSession.getCreationDate();
        assertTrue(creationDate.before(Date.from(Instant.now().plus(1, ChronoUnit.MILLIS))));
    }

    @Test
    public void givenUserSession_whenCreateSameUserSession_thenUpdateUserSession() {
        UserSession userSession = sessionManager.createUserSession(1, new Date());
        String creationDate = userSession.getCreationDate().toString();
        Date updatedDate = Date.from(Instant.now().plus(1, ChronoUnit.SECONDS));
        UserSession userSessionUpdated = sessionManager.createUserSession(1, updatedDate);

        assertEquals(userSessionUpdated.getSessionKey(), userSession.getSessionKey());
        assertNotEquals(userSessionUpdated.getCreationDate().toString(),creationDate);
        assertEquals(userSessionUpdated.getCreationDate(), updatedDate);
    }

    @Test
    public void giveExpiredSession_whenGetUserSession_thenUserIsLogout() {
        UserSession userSession = sessionManager.createUserSession(1, Date.from(Instant.now().minus(12, ChronoUnit.MINUTES)));

        sessionManager.removeUserSession();
        Optional<UserSession> userSessionByKey = sessionManager.getUserSessionByKey(userSession.getSessionKey());

        assertFalse(userSessionByKey.isPresent());
    }

    @Test
    public void whenCreateUserSessionMultiThreading_thenExpectNoConcurrentModificationException() {
        new Assertion<>(
                "Must be able to add multiple scores",
                t -> {
                    boolean success = true;
                    try {
                        sessionManager.createUserSession(t.getAndIncrement(),new Date());
                    } catch (ConcurrentModificationException ex) {
                        success = false;
                    }
                    return success;
                },
                new RunsInThreads<>(new AtomicInteger(), 100)
        ).affirm();
    }

}