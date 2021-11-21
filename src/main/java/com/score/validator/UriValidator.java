package com.score.validator;

import java.util.regex.Pattern;

public class UriValidator {

    private static final Pattern LOGIN = Pattern.compile("/\\d+/login", Pattern.CASE_INSENSITIVE);
    private static final Pattern USER_LEVEL_SCORE = Pattern.compile("/\\d+/score\\?sessionkey=\\w+", Pattern.CASE_INSENSITIVE);
    private static final Pattern HIGH_SCORES_LIST = Pattern.compile("/\\d+/highscorelist", Pattern.CASE_INSENSITIVE);

    public static boolean isValidUserLevelScoreUri(String path) {
        return USER_LEVEL_SCORE.matcher(path).matches();
    }

    public static boolean isValidLoginUri(String path) {
        return LOGIN.matcher(path).matches();
    }

    public static boolean isValidHighScoreUri(String path) {
        return HIGH_SCORES_LIST.matcher(path).matches();
    }

    public static boolean isValidUri(String path) {
        return isValidLoginUri(path) || isValidUserLevelScoreUri(path) || isValidHighScoreUri(path);
    }
}
