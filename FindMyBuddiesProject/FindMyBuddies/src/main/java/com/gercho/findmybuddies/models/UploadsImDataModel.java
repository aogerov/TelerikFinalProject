package com.gercho.findmybuddies.models;

/**
 * Created by Gercho on 11/17/13.
 */
public class UploadsImDataModel {

    private String img_name;
    private String img_url;
    private String img_view;
    private int img_width;
    private int img_height;
    private String img_attr;
    private String img_size;
    private int img_bytes;
    private String thumb_url;
    private int thumb_width;
    private int thumb_height;
    private String source;
    private String resized;
    private String delete_key;

    public UploadsImDataModel(String img_name, String img_url, String img_view, int img_width,
                              int img_height, String img_attr, String img_size, int img_bytes,
                              String thumb_url, int thumb_width, int thumb_height, String source,
                              String resized, String delete_key) {
        this.img_name = img_name;
        this.img_url = img_url;
        this.img_view = img_view;
        this.img_width = img_width;
        this.img_height = img_height;
        this.img_attr = img_attr;
        this.img_size = img_size;
        this.img_bytes = img_bytes;
        this.thumb_url = thumb_url;
        this.thumb_width = thumb_width;
        this.thumb_height = thumb_height;
        this.source = source;
        this.resized = resized;
        this.delete_key = delete_key;
    }

    public String getImg_name() {
        return img_name;
    }

    public void setImg_name(String img_name) {
        this.img_name = img_name;
    }

    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

    public String getImg_view() {
        return img_view;
    }

    public void setImg_view(String img_view) {
        this.img_view = img_view;
    }

    public int getImg_width() {
        return img_width;
    }

    public void setImg_width(int img_width) {
        this.img_width = img_width;
    }

    public int getImg_height() {
        return img_height;
    }

    public void setImg_height(int img_height) {
        this.img_height = img_height;
    }

    public String getImg_attr() {
        return img_attr;
    }

    public void setImg_attr(String img_attr) {
        this.img_attr = img_attr;
    }

    public String getImg_size() {
        return img_size;
    }

    public void setImg_size(String img_size) {
        this.img_size = img_size;
    }

    public int getImg_bytes() {
        return img_bytes;
    }

    public void setImg_bytes(int img_bytes) {
        this.img_bytes = img_bytes;
    }

    public String getThumb_url() {
        return thumb_url;
    }

    public void setThumb_url(String thumb_url) {
        this.thumb_url = thumb_url;
    }

    public int getThumb_width() {
        return thumb_width;
    }

    public void setThumb_width(int thumb_width) {
        this.thumb_width = thumb_width;
    }

    public int getThumb_height() {
        return thumb_height;
    }

    public void setThumb_height(int thumb_height) {
        this.thumb_height = thumb_height;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getResized() {
        return resized;
    }

    public void setResized(String resized) {
        this.resized = resized;
    }

    public String getDelete_key() {
        return delete_key;
    }

    public void setDelete_key(String delete_key) {
        this.delete_key = delete_key;
    }
}
