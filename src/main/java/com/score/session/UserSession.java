package com.score.session;

import java.util.Date;

public class UserSession {
    private final int id;
    private final String sessionKey;
    private Date creationDate;

    public UserSession(int id, String sessionKey, Date creationDate) {
        this.id = id;
        this.sessionKey = sessionKey;
        this.creationDate = creationDate;
    }

    public int getId() {
        return id;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public Date getCreationDate() {
        return (Date) creationDate.clone();
    }

    public void updateCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
}
