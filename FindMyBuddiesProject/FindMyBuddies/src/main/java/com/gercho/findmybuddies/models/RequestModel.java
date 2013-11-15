package com.gercho.findmybuddies.models;

/**
 * Created by Gercho on 11/15/13.
 */
public class RequestModel {

    private int fromUserId;
    private String fromUserNickname;
    private boolean isShowed;

    public RequestModel(int fromUserId, String fromUserNickname, boolean isShowed) {
        this.fromUserId = fromUserId;
        this.fromUserNickname = fromUserNickname;
        this.isShowed = isShowed;
    }

    public int getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(int fromUserId) {
        this.fromUserId = fromUserId;
    }

    public String getFromUserNickname() {
        return fromUserNickname;
    }

    public void setFromUserNickname(String fromUserNickname) {
        this.fromUserNickname = fromUserNickname;
    }

    public boolean isShowed() {
        return isShowed;
    }

    public void setShowed(boolean isShowed) {
        this.isShowed = isShowed;
    }
}
