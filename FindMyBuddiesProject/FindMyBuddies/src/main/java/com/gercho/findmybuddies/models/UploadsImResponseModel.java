package com.gercho.findmybuddies.models;

/**
 * Created by Gercho on 11/17/13.
 */
public class UploadsImResponseModel {

    private int status_code;
    private String status_txt;
    private UploadsImDataModel data;

    public UploadsImResponseModel(int status_code, String status_txt, UploadsImDataModel data) {
        this.status_code = status_code;
        this.status_txt = status_txt;
        this.data = data;
    }

    public int getStatus_code() {
        return status_code;
    }

    public void setStatus_code(int status_code) {
        this.status_code = status_code;
    }

    public String getStatus_txt() {
        return status_txt;
    }

    public void setStatus_txt(String status_txt) {
        this.status_txt = status_txt;
    }

    public UploadsImDataModel getData() {
        return data;
    }

    public void setData(UploadsImDataModel data) {
        this.data = data;
    }
}
