package com.gercho.findmybuddies.validators;

import com.gercho.findmybuddies.helpers.EnumMeasureUnits;
import com.gercho.findmybuddies.helpers.EnumOrderBy;
import com.gercho.findmybuddies.models.FriendModels;

/**
 * Created by Gercho on 11/12/13.
 */
public class BuddiesServiceValidator {

    public static final int UPDATE_FREQUENCY_MIN_MILLISECONDS = 1000 * 60; // 1 minute
    public static final int UPDATE_FREQUENCY_MAX_MILLISECONDS = 1000 * 600; // 10 minutes
    public static final int IMAGES_TO_SHOW_MIN_COUNT = 1;
    public static final int IMAGES_TO_SHOW_MAX_COUNT = 10;
    public static final int SERVER_RESPONSE_MIN_LENGTH = 80;

    public static boolean validateSessionKey(String sessionKey) {
        if (sessionKey.length() != UserServiceValidator.SESSION_KEY_LENGTH) {
            return false;
        }

        return true;
    }

    public static boolean validateUpdateFrequency(int updateFrequency) {
        if (updateFrequency < UPDATE_FREQUENCY_MIN_MILLISECONDS ||
                updateFrequency > UPDATE_FREQUENCY_MAX_MILLISECONDS) {
            return false;
        }

        return true;
    }

    public static boolean validateImagesToShowCount(int imagesToShowCount) {
        if (imagesToShowCount < IMAGES_TO_SHOW_MIN_COUNT ||
                imagesToShowCount > IMAGES_TO_SHOW_MAX_COUNT) {
            return false;
        }

        return true;
    }

    public static boolean validateBuddiesOrderByAsInt(int buddiesOrderBy) {
        if (buddiesOrderBy < 0 || buddiesOrderBy >= EnumOrderBy.values().length) {
            return false;
        }

        return true;
    }

    public static boolean validateDistanceAsInt(int distanceAsInt) {
        if (distanceAsInt < 0 || distanceAsInt >= EnumMeasureUnits.values().length) {
            return false;
        }

        return true;
    }

    public static boolean validateFriendModels(FriendModels friendModels) {
        // TODO validate models
        return true;
    }
}
