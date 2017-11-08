package com.hdl.videopalyerdemo.models;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class CommendListModel {

    private final String BAIDU = "百度百科";
    private final String WEIBO = "微博";
    private final String VIDEO = "点播视频";
    private final String DOUBAN = "豆瓣";
    private final String TAOBAO = "商品";

    private List<CommendModel> baiduModels;
    private List<CommendModel> weiboModels;
    private List<CommendModel> doubanModels;
    private List<CommendModel> dianboModels;
    private List<CommendModel> taobaoModels;

    public CommendListModel(JSONArray array) {
        initAllList();

        if (array != null) {
            for (int i = 0; i < array.length(); ++i) {
                CommendModel commendModel = new CommendModel(array.optJSONObject(i));

                if (BAIDU.equals(commendModel.getData_source())) {
                    baiduModels.add(commendModel);
                }

                if (DOUBAN.equals(commendModel.getData_source())) {
                    doubanModels.add(commendModel);
                }

                if (VIDEO.equals(commendModel.getData_source())) {
                    dianboModels.add(commendModel);
                }

                if (WEIBO.equals(commendModel.getData_source())) {
                    weiboModels.add(commendModel);
                }

                if (TAOBAO.equals(commendModel.getData_source())) {
                    taobaoModels.add(commendModel);
                }
            }
        }
    }

    public List<CommendModel> getModels(int type) {
        List<CommendModel> result = null;
        switch (type) {
            case 0:
                result = baiduModels;
                break;
            case 1:
                result = dianboModels;
                break;
            case 2:
                result = weiboModels;
                break;
            case 3:
                result = doubanModels;
                break;
            case 4:
                result = taobaoModels;
                break;
        }
        
        return result;
    }

    private void initAllList() {
        if (baiduModels != null) {
            baiduModels.clear();
        } else {
            baiduModels = new ArrayList<>();
        }

        if (weiboModels != null) {
            weiboModels.clear();
        } else {
            weiboModels = new ArrayList<>();
        }

        if (doubanModels != null) {
            doubanModels.clear();
        } else {
            doubanModels = new ArrayList<>();
        }

        if (dianboModels != null) {
            dianboModels.clear();
        } else {
            dianboModels = new ArrayList<>();
        }

        if (taobaoModels != null) {
            taobaoModels.clear();
        } else {
            taobaoModels = new ArrayList<>();
        }
    }
}
