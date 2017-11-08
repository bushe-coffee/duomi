package com.hdl.videopalyerdemo.models;

import org.json.JSONObject;


public class ProductRectangle {

    private double x;
    private double y;
    private double height;
    private double width;

    public ProductRectangle(JSONObject object1) {
        if (object1 != null) {
            x = object1.optDouble("x");
            y = object1.optDouble("y");
            height = object1.optDouble("height");
            width = object1.optDouble("width");
        }
    }
}
