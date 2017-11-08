package com.hdl.videopalyerdemo.models;


import org.json.JSONObject;

public class ProductModel {

    private String url;
    private String product_image;
    private String product_name;
    private double price;

    public ProductModel(JSONObject object) {
        if (object != null) {
            url = object.optString("url");
            product_image = object.optString("product_image");
            product_name = object.optString("product_name");
            price = object.optDouble("price");
        }
    }

    public String getUrl() {
        return url;
    }

    public String getProduct_image() {
        return product_image;
    }

    public String getProduct_name() {
        return product_name;
    }

    public double getPrice() {
        return price;
    }
}
