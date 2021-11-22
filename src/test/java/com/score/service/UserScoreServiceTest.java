package com.score.service;

import com.score.model.UserScore;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.llorllale.cactoos.matchers.Assertion;
import org.llorllale.cactoos.matchers.RunsInThreads;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class UserScoreServiceTest {

    private UserScoreService userScoreService;

    @Before
    public void setUp() {
        userScoreService = UserScoreService.getInstance();
    }

    @After
    public void tearDown() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        var map = reflectionMethodGetUserLevelScores();
        map.clear();
    }

    @Test
    public void givenNoInstance_whenGetInstanceMultipleTimes_thenReturnSameInstance() {
        assertSame(UserScoreService.getInstance(), UserScoreService.getInstance());
    }

    @Test
    public void givenNoUserScore_whenAddUserScoreForLevel_thenReturnOnlyOneUserScore() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        var userScore = new UserScore(1, 10);
        userScoreService.addScore(1, userScore);

        var getUserScores = reflectionMethodGetUserLevelScores();
        assertNotNull(getUserScores);
        assertTrue(getUserScores.containsKey(1));
        assertEquals(getUserScores.get(1).first(), userScore);
    }

    @Test
    public void givenUserScoreForLevel_whenAddNewScoreForSameUserAndSameLevel_thenUpdate() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {

        var userScoreUpdated = new UserScore(1, 20);
        userScoreService.addScore(1, userScoreUpdated);

        var getUserScores = reflectionMethodGetUserLevelScores();
        assertNotNull(getUserScores);
        assertTrue(getUserScores.containsKey(1));
        assertEquals(getUserScores.get(1).size(), 1);
        assertEquals(getUserScores.get(1).first(), userScoreUpdated);
    }

    @Test
    public void givenUserScoreForLevel_whenAddDifferentUserScoreForExistingLevel_thenExpect2USerScoresForSameLevel() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        var givenScenario = getGivenScenario(Collections.singletonList(new UserScore(1, 20)));

        reflectionSetUserLevelScores(givenScenario);

        var userScoreUpdated = new UserScore(2, 10);
        userScoreService.addScore(1, userScoreUpdated);

        var getUserScores = reflectionMethodGetUserLevelScores();
        assertNotNull(getUserScores);
        assertTrue(getUserScores.containsKey(1));
        assertEquals(getUserScores.get(1).size(), 2);
        assertEquals(getUserScores.get(1).last(), userScoreUpdated);
    }

    @Test
    public void givenUserScoreForLevel_whenAddSameUserScoreForDifferentLevel_thenExpectUserScoredToBeAdded() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        reflectionSetUserLevelScores(getGivenScenario(Collections.singletonList(new UserScore(1, 20))));

        UserScore userScoreUpdated = new UserScore(2, 10);
        userScoreService.addScore(2, userScoreUpdated);

        var getUserScores = reflectionMethodGetUserLevelScores();
        assertNotNull(getUserScores);
        assertTrue(getUserScores.containsKey(2));
        assertEquals(getUserScores.get(2).size(), 1);
        assertEquals(getUserScores.get(2).last(), userScoreUpdated);
    }

    @Test
    public void givenFullScoreBoardForLevel_whenAddUserScoresForSameLevel_thenExpectScoreBoardSameSize() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        reflectionSetUserLevelScores(getGivenScenario(getUserScores()));

        userScoreService.addScore(1, new UserScore(16, 25));

        var getUserScores = reflectionMethodGetUserLevelScores();
        assertEquals(15, getUserScores.get(1).size());
    }

    @Test
    public void giveAddedUserScores_whenGetHighScoresForSameLevel_thenReturnCsvFormat() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        reflectionSetUserLevelScores(getGivenScenario(Arrays.asList(new UserScore(1, 10), new UserScore(2, 20), new UserScore(3, 15))));

        assertNotNull(userScoreService.getHighScoreList(1));
        assertFalse(userScoreService.getHighScoreList(1).isEmpty());

        assertEquals(userScoreService.getHighScoreList(1), "2=20,3=15,1=10");

    }

    @Test
    public void whenAddScoreMultiThreading_thenExpectNoConcurrentModificationException() {
        new Assertion<>(
                "Must be able to add multiple scores simultaneous",
                t -> {
                    boolean success = true;
                    try {
                        int getAndIncrement = t.getAndIncrement();
                        userScoreService.addScore(getAndIncrement % 10, new UserScore(getAndIncrement, getAndIncrement + 10));
                    } catch (ConcurrentModificationException ex) {
                        success = false;
                    }
                    return success;
                },
                new RunsInThreads<>(new AtomicInteger(), 100)
        ).affirm();
    }


    private void reflectionSetUserLevelScores(ConcurrentHashMap<Integer, ConcurrentSkipListSet<UserScore>> givenScenario) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method method = UserScoreService.class.getDeclaredMethod("setUserLevelScores", ConcurrentHashMap.class);
        method.setAccessible(true);
        method.invoke(userScoreService, givenScenario);
    }

    private ConcurrentHashMap<Integer, ConcurrentSkipListSet<UserScore>> reflectionMethodGetUserLevelScores() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method method = UserScoreService.class.getDeclaredMethod("getUserLevelScores");
        method.setAccessible(true);
        return (ConcurrentHashMap<Integer, ConcurrentSkipListSet<UserScore>>) method.invoke(userScoreService);
    }

    private ConcurrentHashMap<Integer, ConcurrentSkipListSet<UserScore>> getGivenScenario(List<UserScore> userScoreList) {
        ConcurrentHashMap<Integer, ConcurrentSkipListSet<UserScore>> userLevelScores = new ConcurrentHashMap<>();
        var userScores = new ConcurrentSkipListSet<>(Comparator.comparingInt(UserScore::getScore).reversed().thenComparingInt(UserScore::getId));
        userScores.addAll(userScoreList);
        userLevelScores.putIfAbsent(1, userScores);
        return userLevelScores;
    }

    private List<UserScore> getUserScores() {
        List<UserScore> userScores = new ArrayList<>();
        for (int i = 14; i >= 1; i--) {
            userScores.add(new UserScore(i, i + 10));
        }
        return userScores;
    }
}