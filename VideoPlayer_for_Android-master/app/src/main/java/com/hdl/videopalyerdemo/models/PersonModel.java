package com.hdl.videopalyerdemo.models;


import org.json.JSONObject;

public class PersonModel {

    private String star_name;

    PersonModel(JSONObject object) {
        if (object != null) {
            star_name = object.optString("star_name");
        }
    }

    public String getStar_name() {
        return star_name;
    }
}
