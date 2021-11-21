package com.score.service;

import com.score.model.UserScore;

import java.util.Comparator;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

public class UserScoreService {
    private final Integer HIGH_SCORE_LEVEL_NO = 15;

    private static final UserScoreService instance = new UserScoreService();
    private ConcurrentHashMap<Integer, ConcurrentSkipListSet<UserScore>> userLevelScores; // level -> userScores

    private UserScoreService() {
        this.userLevelScores = new ConcurrentHashMap<>();
    }

    public static UserScoreService getInstance() {
        return instance;
    }


    public synchronized void addScore(int level, UserScore userScore) {
        var userScores = this.userLevelScores.get(level);
        if (Objects.nonNull(userScores)) {
            if (userScores.stream().anyMatch(u -> u.getId() == userScore.getId())) {
                updateUserScoreIfNeeded(userScore, level);
            } else if (userScores.size() >= HIGH_SCORE_LEVEL_NO && userScores.last().getScore() < userScore.getScore()) {
                //TODO this should be atomic
                userScores.remove(userScores.last());
                userScores.add(userScore);
            } else {
                userScores.add(userScore);
            }
        }
        addNewUserScore(level, userScore);
    }

    public String getHighScoreList(int level) {
        String highScoreResponse = "";
        ConcurrentSkipListSet<UserScore> userScores = this.userLevelScores.get(level);
        if (Objects.nonNull(userScores)) {
            highScoreResponse = userScores.stream().map(userScore -> userScore.getId() + "=" + userScore.getScore()).collect(Collectors.joining(","));
        }
        return highScoreResponse;
    }

    private ConcurrentHashMap<Integer, ConcurrentSkipListSet<UserScore>> getUserLevelScores() {
        return userLevelScores;
    }

    private void setUserLevelScores(ConcurrentHashMap<Integer, ConcurrentSkipListSet<UserScore>> userLevelScores) {
        this.userLevelScores = userLevelScores;
    }

    private void addNewUserScore(int level, UserScore userScore) {
        ConcurrentSkipListSet<UserScore> userScores;
        userScores = new ConcurrentSkipListSet<>(Comparator.comparingInt(UserScore::getScore).reversed().thenComparingInt(UserScore::getId));
        userScores.add(userScore);
        userLevelScores.putIfAbsent(level, userScores);
    }

    private void updateUserScoreIfNeeded(UserScore userScore, Integer level) {
        ConcurrentSkipListSet<UserScore> userScores = this.userLevelScores.get(level);
        //TODO this should be atomic
        if (userScores.removeIf(u -> u.getId() == userScore.getId() && u.getScore() < userScore.getScore())) {
            userScores.add(userScore);
        }
    }
}
