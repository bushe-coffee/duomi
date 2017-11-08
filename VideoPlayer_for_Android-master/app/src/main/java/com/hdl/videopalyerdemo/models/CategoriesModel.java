package com.hdl.videopalyerdemo.models;

import org.json.JSONObject;


public class CategoriesModel {

    private String category_name;
    private int confidence;

    CategoriesModel(JSONObject object) {
        if (object != null) {
            category_name = object.optString("category_name");
            confidence = object.optInt("confidence");
        }
    }

    public String getCategory_name() {
        return category_name;
    }

    public int getConfidence() {
        return confidence;
    }
}
