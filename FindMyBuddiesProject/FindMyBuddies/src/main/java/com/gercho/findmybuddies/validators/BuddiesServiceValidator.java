package com.gercho.findmybuddies.validators;

import com.gercho.findmybuddies.broadcasts.BuddiesServiceBroadcast;
import com.gercho.findmybuddies.helpers.OrderBy;

/**
 * Created by Gercho on 11/12/13.
 */
public class BuddiesServiceValidator {

    public static final int UPDATE_FREQUENCY_MIN_MILLISECONDS = 1000 * 20; // 20 seconds
    public static final int UPDATE_FREQUENCY_MAX_MILLISECONDS = 1000 * 600; // 10 minutes
    public static final int IMAGES_TO_SHOW_MIN_COUNT = 1;
    public static final int IMAGES_TO_SHOW_MAX_COUNT = 10;

    private BuddiesServiceBroadcast mBroadcast;

    public BuddiesServiceValidator(BuddiesServiceBroadcast broadcast) {
        this.mBroadcast = broadcast;
    }

    public boolean validateSesionKey (String sessionKey) {
        if (sessionKey.length() != UserServiceValidator.SESSION_KEY_LENGTH) {
            return false;
        }

        return true;
    }

    public boolean validateUpdateFrequency(int updateFrequency) {
        if (updateFrequency < UPDATE_FREQUENCY_MIN_MILLISECONDS ||
                updateFrequency > UPDATE_FREQUENCY_MAX_MILLISECONDS) {
            return false;
        }

        return true;
    }

    public boolean validateImagesToShowCount(int imagesToShowCount) {
        if (imagesToShowCount < IMAGES_TO_SHOW_MIN_COUNT ||
                imagesToShowCount > IMAGES_TO_SHOW_MAX_COUNT) {
            return false;
        }

        return true;
    }

    public boolean validateBuddiesOrderByAsInt(int buddiesOrderBy) {
        if (buddiesOrderBy < 0 || buddiesOrderBy > OrderBy.values().length) {
            return false;
        }

        return true;
    }
}