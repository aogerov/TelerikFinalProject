package com.gercho.findmybuddies.models;

import java.util.ArrayList;

/**
 * Created by Gercho on 11/13/13.
 */
public class FriendModels { // implements Parcelable

    private ArrayList<FriendModel> friends;

    public FriendModels(ArrayList<FriendModel> friends){
        this.friends = friends;
    }

    public ArrayList<FriendModel> getFriends() {
        return friends;
    }

    public void setFriends(ArrayList<FriendModel> friends) {
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
