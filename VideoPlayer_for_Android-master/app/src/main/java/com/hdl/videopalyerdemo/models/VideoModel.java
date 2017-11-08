package com.hdl.videopalyerdemo.models;

import org.json.JSONObject;

public class VideoModel {
    private long create_time;
    private double video_size;
    private String video_url;
    private String video_name;

    public VideoModel(JSONObject object) {
        if (object != null) {
            create_time = object.optLong("create_time");
            video_size = object.optDouble("video_size");
            video_url = object.optString("video_url");
            video_name = object.optString("video_name");
        }
    }

    public long getCreate_time() {
        return create_time;
    }

    public double getVideo_size() {
        return video_size;
    }

    public String getVideo_url() {
        return video_url;
    }

    public String getVideo_name() {
        return video_name;
    }
}
