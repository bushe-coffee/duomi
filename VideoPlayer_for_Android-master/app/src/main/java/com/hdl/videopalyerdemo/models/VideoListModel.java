package com.hdl.videopalyerdemo.models;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class VideoListModel {

    private List<VideoModel> mVideoList;

    public VideoListModel(JSONArray array) {
        if (array != null) {
            mVideoList = new ArrayList<>();
            for (int i = 0; i < array.length(); ++i) {
                VideoModel videoModel = new VideoModel(array.optJSONObject(i));
                mVideoList.add(videoModel);
            }
        }
    }

    public List<VideoModel> getVideoList() {
        return mVideoList;
    }
}
