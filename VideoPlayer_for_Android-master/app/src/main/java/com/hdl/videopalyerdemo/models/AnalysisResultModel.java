package com.hdl.videopalyerdemo.models;


import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AnalysisResultModel {

    private List<ScenesModel> sceneList;
    private List<CategoriesModel> categoriesList;
    private FacesModel faces;

    public AnalysisResultModel(JSONObject object) {

        JSONArray sce = object.optJSONArray("scenes");
        if (sce != null && sce.length() > 0) {
            sceneList = new ArrayList<>();
            for (int i=0;i<sce.length();++i) {
                ScenesModel model = new ScenesModel(sce.optJSONObject(i));
                sceneList.add(model);
            }
        }

        JSONArray cate = object.optJSONArray("categories");
        if (cate != null && cate.length() > 0) {
            categoriesList = new ArrayList<>();
            for (int i=0;i<cate.length();++i) {
                CategoriesModel model = new CategoriesModel(cate.optJSONObject(i));
                categoriesList.add(model);
            }
        }

        JSONObject fa = object.optJSONObject("faces");
        if (fa != null) {
            faces = new FacesModel(fa);
        }
    }

    public List<ScenesModel> getSceneList() {
        return sceneList;
    }

    public List<CategoriesModel> getCategoriesList() {
        return categoriesList;
    }

    public FacesModel getFaces() {
        return faces;
    }
}
