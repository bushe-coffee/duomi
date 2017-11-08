package com.hdl.videopalyerdemo.models;


import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ProductResultModels {

    private List<ProductModel> product_list;
    private ProductRectangle rectangle;

    public ProductResultModels(JSONObject object) {

        if (product_list == null) {
            product_list = new ArrayList<>();
        } else {
            product_list.clear();
        }

        JSONArray array = object.optJSONArray("product_list");
        for (int i=0; i< array.length();++i) {
            ProductModel model = new ProductModel(array.optJSONObject(i));
            product_list.add(model);
        }

        JSONObject object1 = object.optJSONObject("rectangle");
        rectangle = new ProductRectangle(object1);
    }

    public List<ProductModel> getProductList() {
        return product_list;
    }
}
