package com.gercho.findmybuddies.data;

import com.gercho.findmybuddies.enums.OrderByTypes;

/**
 * Created by Gercho on 11/16/13.
 */
public class DataPersister {

    public static HttpResponse validateSessionKey(String sessionKey) {
        return HttpRequester.get(String.format(
                "users/validate?sessionKey=%s",
                sessionKey));
    }

    public static HttpResponse login(String userModelAsJson) {
        return HttpRequester.post(
                "users/login",
                userModelAsJson);
    }

    public static HttpResponse register(String userModelAsJson) {
        return HttpRequester.post(
                "users/register",
                userModelAsJson);
    }

    public static HttpResponse logout(String sessionKey) {
        return HttpRequester.get(String.format(
                "users/logout?sessionKey=%s",
                sessionKey));
    }

    public static HttpResponse getAllBuddies(OrderByTypes orderByType, String sessionKey) {
        return HttpRequester.get(String.format(
                "friends/all?orderBy=%s&sessionKey=%s",
                orderByType.toString().toLowerCase(), sessionKey));
    }

    public static HttpResponse getNewRequests(String sessionKey) {
        return HttpRequester.get(String.format(
                "requests/newRequestsCount?sessionKey=%s",
                sessionKey));
    }

    public static HttpResponse updateCurrentPosition(String sessionKey, String coordinatesModelAsJson) {
        return HttpRequester.post(String.format(
                "coordinates/update?sessionKey=%s",
                sessionKey),
                coordinatesModelAsJson);

    }

    public static HttpResponse removeExistingBuddie(String sessionKey, String buddieAsJson) {
        return HttpRequester.post(String.format(
                "friends/remove?sessionKey=%s",
                sessionKey),
                buddieAsJson);

    }

    public static HttpResponse searchForNewBuddie(String buddieNickname, String sessionKey) {
        return HttpRequester.get(String.format(
                "friends/find?friendNickname=%s&sessionKey=%s",
                buddieNickname, sessionKey));
    }

    public static HttpResponse getAllRequests(String sessionKey) {
        return HttpRequester.get(String.format(
                "requests/all?sessionKey=%s",
                sessionKey));
    }

    public static HttpResponse sendBuddieRequest(String sessionKey, String buddieAsJson) {
        return HttpRequester.post(String.format(
                "requests/add?sessionKey=%s",
                sessionKey),
                buddieAsJson);
    }

    public static HttpResponse respondToBuddieRequest(String sessionKey, String responseAsJson) {
        return HttpRequester.post(String.format(
                "requests/response?sessionKey=%s",
                sessionKey),
                responseAsJson);
    }

    public static HttpResponse sendNewImage() {
        // TODO fill
        return null;
    }

    public static HttpResponse getBuddieImages(int imagesToShowCount, String sessionKey, String buddieAsJson) {
        return HttpRequester.post(String.format(
                "images/get?imagesCount=%s&sessionKey=%s",
                imagesToShowCount, sessionKey),
                buddieAsJson);
    }
}
