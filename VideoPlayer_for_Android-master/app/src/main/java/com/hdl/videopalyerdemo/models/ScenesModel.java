package com.hdl.videopalyerdemo.models;


import org.json.JSONObject;

public class ScenesModel {

    private String scene_name;
    private int confidence;

    ScenesModel(JSONObject object) {
        if (object != null) {
            scene_name = object.optString("scene_name");
            confidence = object.optInt("confidence");
        }
    }

    public String getScene_name() {
        return scene_name;
    }

    public int getConfidence() {
        return confidence;
    }
}
