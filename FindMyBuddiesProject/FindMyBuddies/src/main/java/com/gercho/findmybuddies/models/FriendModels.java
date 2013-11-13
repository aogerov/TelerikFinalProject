package com.gercho.findmybuddies.models;

/**
 * Created by Gercho on 11/13/13.
 */
public class FriendModels { // implements Parcelable

    private FriendModel[] friends;

    public FriendModels(FriendModel[] friends) {
        this.friends = friends;
    }

    public FriendModel[] getFriends() {
        return friends;
    }

    public void setFriends(FriendModel[] friends) {
        this.friends = friends;
    }

    // TODO try to make this on intent.putExtra();
//
//    @Override
//    public int describeContents() {
//        return 0;
//    }
//
//    @Override
//    public void writeToParcel(Parcel parcel, int i) {
//
//    }
}
