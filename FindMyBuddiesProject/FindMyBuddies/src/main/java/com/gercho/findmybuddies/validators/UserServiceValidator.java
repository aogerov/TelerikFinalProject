package com.gercho.findmybuddies.validators;

import android.content.Intent;

import com.gercho.findmybuddies.broadcasts.UserServiceBroadcast;
import com.gercho.findmybuddies.http.HttpResponse;
import com.gercho.findmybuddies.models.UserModel;
import com.gercho.findmybuddies.services.UserService;

/**
 * Created by Gercho on 11/10/13.
 */
public class UserServiceValidator {

    public static final int MIN_USERNAME_AND_NICKNAME_LENGTH = 3;
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MAX_INPUT_FIELDS_LENGTH = 30;
    public static final int SESSION_KEY_LENGTH = 40;
    public static final int SERVER_RESPONSE_MIN_LENGTH = 70;

    private UserServiceBroadcast mBroadcast;

    public UserServiceValidator(UserServiceBroadcast broadcast) {
        this.mBroadcast = broadcast;
    }

    public String extractAndValidateUsername(Intent intent) {
        String username = intent.getStringExtra(UserService.USERNAME_EXTRA);
        if (username != null) {
            String usernameTrimmed = username.trim();
            if (usernameTrimmed.length() >= MIN_USERNAME_AND_NICKNAME_LENGTH &&
                    usernameTrimmed.length() <= MAX_INPUT_FIELDS_LENGTH) {
                return usernameTrimmed;
            }
        }

        this.mBroadcast.sendResponseMessage(
                String.format("Username must be min %d and max %d chars long",
                        MIN_USERNAME_AND_NICKNAME_LENGTH, MAX_INPUT_FIELDS_LENGTH));
        return null;
    }

    public String extractAndValidateNickname(Intent intent) {
        String nickname = intent.getStringExtra(UserService.NICKNAME_EXTRA);
        if (nickname != null) {
            String nicknameTrimmed = nickname.trim();
            if (nicknameTrimmed.length() >= MIN_USERNAME_AND_NICKNAME_LENGTH &&
                    nicknameTrimmed.length() <= MAX_INPUT_FIELDS_LENGTH) {
                return nicknameTrimmed;
            }
        }

        this.mBroadcast.sendResponseMessage(
                String.format("Nickname must be min %d and max %d chars long",
                        MIN_USERNAME_AND_NICKNAME_LENGTH, MAX_INPUT_FIELDS_LENGTH));
        return null;
    }

    public String extractAndValidatePassword(Intent intent) {
        String password = intent.getStringExtra(UserService.PASSWORD_EXTRA);
        if (password != null) {
            String passwordTrimmed = password.trim();
            if (passwordTrimmed.length() >= MIN_PASSWORD_LENGTH &&
                    passwordTrimmed.length() <= MAX_INPUT_FIELDS_LENGTH) {
                return passwordTrimmed;
            }
        }

        this.mBroadcast.sendResponseMessage(
                String.format("Password must be min %d and max %d chars long",
                        MIN_PASSWORD_LENGTH, MAX_INPUT_FIELDS_LENGTH));
        return null;
    }

    public boolean validateHttpResponse(HttpResponse response) {
        if (response == null || response.getMessage() == null ||
                response.getMessage().length() < SERVER_RESPONSE_MIN_LENGTH) {
            return false;
        }

        return true;
    }

    public boolean validateUserModel(UserModel userModel) {
        String nickname = userModel.getNickname();
        String sessionKey = userModel.getSessionKey();
        if (nickname.length() < MIN_USERNAME_AND_NICKNAME_LENGTH ||
                nickname.length() > MAX_INPUT_FIELDS_LENGTH ||
                sessionKey.length() != SESSION_KEY_LENGTH) {
            return false;
        }

        return true;
    }
}
