package com.gercho.findmybuddies.validators;

import com.gercho.findmybuddies.http.HttpResponse;
import com.gercho.findmybuddies.models.UserModel;

/**
 * Created by Gercho on 11/10/13.
 */
public class UserServiceValidator {

    public static final int MIN_USERNAME_AND_NICKNAME_LENGTH = 3;
    public static final int MIN_PASSWORD_LENGTH = 6;
    public static final int MAX_INPUT_FIELDS_LENGTH = 30;
    public static final int SESSION_KEY_LENGTH = 40;
    public static final int SERVER_RESPONSE_MIN_LENGTH = 70;

    public static boolean validateUsername(String username) {
        return validateNames(username);
    }

    public static boolean validateNickname(String nickname) {
        return validateNames(nickname);
    }

    public static boolean validateNames(String name) {
        if (name != null) {
            String nameTrimmed = name.trim();
            if (nameTrimmed.length() >= MIN_USERNAME_AND_NICKNAME_LENGTH &&
                    nameTrimmed.length() <= MAX_INPUT_FIELDS_LENGTH) {
                return true;
            }
        }
        return false;
    }

    public static boolean validatePassword(String password) {
        if (password != null) {
            String passwordTrimmed = password.trim();
            if (passwordTrimmed.length() >= MIN_PASSWORD_LENGTH &&
                    passwordTrimmed.length() <= MAX_INPUT_FIELDS_LENGTH) {
                return true;
            }
        }

        return false;
    }

    public static boolean validateHttpResponse(HttpResponse response) {
        if (response == null || response.getMessage() == null ||
                response.getMessage().length() < SERVER_RESPONSE_MIN_LENGTH) {
            return false;
        }

        return true;
    }

    public static boolean validateUserModel(UserModel userModel) {
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
