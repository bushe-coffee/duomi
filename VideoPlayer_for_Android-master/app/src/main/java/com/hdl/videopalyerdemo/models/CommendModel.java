package com.hdl.videopalyerdemo.models;


import org.json.JSONObject;

public class CommendModel {

    private String project_name;
    private String comment1;
    private String comment2;
    private String comment3;
    private String detailed_image_url;
    private String display_title;
    private String detailed_title;
    private String cast;
    private String tag_name;
    private String data_source;
    private String detailed_description;
    private String display_douban;
    private String display_brief;
    private String display_image_url;
    private String video_name;
    private int id;

    CommendModel(JSONObject object) {
        if (object != null) {
            project_name = object.optString("project_name");
            comment1 = object.optString("comment1");
            comment2 = object.optString("comment2");
            comment3 = object.optString("comment3");
            detailed_image_url = object.optString("detailed_image_url");
            display_title = object.optString("display_title");
            detailed_title = object.optString("detailed_title");
            cast = object.optString("cast");
            tag_name = object.optString("tag_name");
            data_source = object.optString("data_source");
            detailed_description = object.optString("detailed_description");
            display_douban = object.optString("display_douban");
            display_brief = object.optString("display_brief");
            display_image_url = object.optString("display_image_url");
            video_name = object.optString("video_name");
            id = object.optInt("id");
        }
    }

    public String getProject_name() {
        return project_name;
    }

    public String getComment1() {
        return comment1;
    }

    public String getComment2() {
        return comment2;
    }

    public String getComment3() {
        return comment3;
    }

    public String getDetailed_image_url() {
        return detailed_image_url;
    }

    public String getDisplay_title() {
        return display_title;
    }

    public String getDetailed_title() {
        return detailed_title;
    }

    public String getCast() {
        return cast;
    }

    public String getTag_name() {
        return tag_name;
    }

    public String getData_source() {
        return data_source;
    }

    public String getDetailed_description() {
        return detailed_description;
    }

    public String getDisplay_douban() {
        return display_douban;
    }

    public String getDisplay_brief() {
        return display_brief;
    }

    public String getDisplay_image_url() {
        return display_image_url;
    }

    public String getVideo_name() {
        return video_name;
    }

    public int getId() {
        return id;
    }
}
