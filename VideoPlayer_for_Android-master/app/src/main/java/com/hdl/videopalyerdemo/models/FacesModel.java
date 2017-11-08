package com.hdl.videopalyerdemo.models;


import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FacesModel {

    private int face_counts;
    private List<PersonModel> face_attribute;

    public FacesModel(JSONObject object) {
        if (object != null) {
            face_counts = object.optInt("face_counts");
            face_attribute = new ArrayList<>();
            JSONArray array = object.optJSONArray("face_attribute");
            for (int i=0;array != null && i<array.length();++i) {
                PersonModel person = new PersonModel(array.optJSONObject(i));
                face_attribute.add(person);
            }
        }
    }

    public int getFace_counts() {
        return face_counts;
    }

    public List<PersonModel> getFace_attribute() {
        return face_attribute;
    }
}
