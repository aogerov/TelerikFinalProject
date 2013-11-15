package com.gercho.findmybuddies.models;

/**
 * Created by Gercho on 11/16/13.
 */
public class ResponseModel {

    private int fromUserId;
    private String fromUserNickname;
    private boolean isAccepted;
    private boolean isLeftForLater;

    public ResponseModel(int fromUserId, String fromUserNickname, boolean isAccepted, boolean isLeftForLater) {
        this.fromUserId = fromUserId;
        this.fromUserNickname = fromUserNickname;
        this.isAccepted = isAccepted;
        this.isLeftForLater = isLeftForLater;
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

    public boolean isAccepted() {
        return isAccepted;
    }

    public void setAccepted(boolean isAccepted) {
        this.isAccepted = isAccepted;
    }

    public boolean isLeftForLater() {
        return isLeftForLater;
    }

    public void setLeftForLater(boolean isLeftForLater) {
        this.isLeftForLater = isLeftForLater;
    }
}
