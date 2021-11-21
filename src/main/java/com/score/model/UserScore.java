package com.score.model;

public class UserScore {

    private final int id;
    private final int score;

    public UserScore(int id,int  score) {
        this.id = id;
        this.score = score;
    }

    public int getId() {
        return id;
    }

    public int getScore() {
        return score;
    }
}
