package com.gercho.findmybuddies.models;

/**
 * Created by Gercho on 11/10/13.
 */
public class UserModel {

    private String username;
    private String authCode;
    private String nickname;
    private String sessionKey;

    public UserModel(String username, String authCode, String nickname) {
        this.username = username;
        this.authCode = authCode;
        this.nickname = nickname;
    }

    public UserModel(String username, String authCode) {
        this.username = username;
        this.authCode = authCode;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAuthCode() {
        return this.authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public String getNickname() {
        return this.nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }
}
