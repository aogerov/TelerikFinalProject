package com.gercho.findmybuddies.models;

import java.util.ArrayList;

/**
 * Created by Gercho on 11/13/13.
 */
public class FriendModels { // implements Parcelable

    private ArrayList<FriendModel> onlineFriends;
    private ArrayList<FriendModel> offlineFriends;

    public FriendModels(){
        this.onlineFriends = new ArrayList<FriendModel>();
        this.offlineFriends = new ArrayList<FriendModel>();
    }

    public ArrayList<FriendModel> getOnlineFriends() {
        return onlineFriends;
    }

    public void setOnlineFriends(ArrayList<FriendModel> onlineFriends) {
        this.onlineFriends = onlineFriends;
    }

    public ArrayList<FriendModel> getOfflineFriends() {
        return offlineFriends;
    }

    public void setOfflineFriends(ArrayList<FriendModel> offlineFriends) {
        this.offlineFriends = offlineFriends;
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
