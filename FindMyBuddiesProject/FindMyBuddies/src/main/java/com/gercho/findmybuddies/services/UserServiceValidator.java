package com.gercho.findmybuddies.services;

import android.content.Intent;

/**
 * Created by Gercho on 11/10/13.
 */
public class UserServiceValidator {

    private static final int MIN_USERNAME_AND_NICKNAME_LENGTH = 3;
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_INPUT_FIELDS_LENGTH = 30;

    private UserServiceBroadcastManager mBroadcastManager;

    public UserServiceValidator(UserServiceBroadcastManager broadcastManager) {
        this.mBroadcastManager = broadcastManager;
    }

    public String extractAndValidateUsername(Intent intent) {
        String username = intent.getStringExtra(UserService.USERNAME);
        if (username != null) {
            String usernameTrimmed = username.trim();
            if (usernameTrimmed.length() >= MIN_USERNAME_AND_NICKNAME_LENGTH &&
                    usernameTrimmed.length() <= MAX_INPUT_FIELDS_LENGTH) {
                return usernameTrimmed;
            }
        }

        this.mBroadcastManager.sendResponseMessage(
                String.format("Username must be min %d and max %d chars long",
                        MIN_USERNAME_AND_NICKNAME_LENGTH, MAX_INPUT_FIELDS_LENGTH));
        return null;
    }

    public String extractAndValidateNickname(Intent intent) {
        String nickname = intent.getStringExtra(UserService.NICKNAME);
        if (nickname != null) {
            String nicknameTrimmed = nickname.trim();
            if (nicknameTrimmed.length() >= MIN_USERNAME_AND_NICKNAME_LENGTH &&
                    nicknameTrimmed.length() <= MAX_INPUT_FIELDS_LENGTH) {
                return nicknameTrimmed;
            }
        }

        this.mBroadcastManager.sendResponseMessage(
                String.format("Nickname must be min %d and max %d chars long",
                        MIN_USERNAME_AND_NICKNAME_LENGTH, MAX_INPUT_FIELDS_LENGTH));
        return null;
    }

    public String extractAndValidatePassword(Intent intent) {
        String password = intent.getStringExtra(UserService.PASSWORD);
        if (password != null) {
            String passwordTrimmed = password.trim();
            if (passwordTrimmed.length() >= MIN_PASSWORD_LENGTH &&
                    passwordTrimmed.length() <= MAX_INPUT_FIELDS_LENGTH) {
                return passwordTrimmed;
            }
        }

        this.mBroadcastManager.sendResponseMessage(
                String.format("Password must be min %d and max %d chars long",
                        MIN_PASSWORD_LENGTH, MAX_INPUT_FIELDS_LENGTH));
        return null;
    }
}
