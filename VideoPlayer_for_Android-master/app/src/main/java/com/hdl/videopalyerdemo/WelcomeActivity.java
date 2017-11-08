package com.hdl.videopalyerdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hdl.videopalyerdemo.models.VideoListModel;
import com.hdl.videopalyerdemo.models.VideoModel;
import com.hdl.videopalyerdemo.utils.NetWorkCallback;
import com.hdl.videopalyerdemo.utils.NetWorkUtils;
import com.hdl.videopalyerdemo.utils.YiPlusUtilities;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

// 咪咕 可以 截图 ，但是 播放器 太差
public class WelcomeActivity extends Activity implements View.OnClickListener, View.OnFocusChangeListener {

    private List<VideoModel> mListVideos;

    private LinearLayout mLayoutContainer;

    private int screenWidth;
    private int screenHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);
        screenWidth = YiPlusUtilities.getScreenWidth(this);
        screenHeight = YiPlusUtilities.getScreenHeight(this);
        mLayoutContainer = (LinearLayout) findViewById(R.id.content_grid);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mLayoutContainer.getLayoutParams();
        params.width = (int) (screenWidth * 0.9);
        params.height = (int) (screenHeight * 0.7);
        mLayoutContainer.setLayoutParams(params);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mListVideos == null || mListVideos.size() == 0) {
            requestData();
        }
    }

    private void requestData() {
        String time = (System.currentTimeMillis() / 1000) + "";
        String data = YiPlusUtilities.getPostParams(time);
        NetWorkUtils.post(YiPlusUtilities.LIST_URL, data, null, new NetWorkCallback() {
            @Override
            public void onServerResponse(Bundle result) {
                try {
                    String res = (String) result.get("result");
                    System.out.println("Yi Plus  videolist " + res);
                    if (!YiPlusUtilities.isStringNullOrEmpty(res)) {
                        JSONArray array = new JSONArray(res);
                        VideoListModel model = new VideoListModel(array);
                        mListVideos = model.getVideoList();
                        if (mListVideos != null) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    for (int i= 0;i<mListVideos.size();++i) {
                                        View view = LayoutInflater.from(WelcomeActivity.this).inflate(R.layout.item_grid_layout, mLayoutContainer, false);
                                        setDataAboutVideo(mListVideos.get(i), view);
                                        mLayoutContainer.addView(view);
                                    }
                                }
                            });
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void setDataAboutVideo(VideoModel model, View parent) {
        RelativeLayout mContainer = (RelativeLayout) parent.findViewById(R.id.item_grid_container);
        ImageButton mBackground = (ImageButton) parent.findViewById(R.id.item_grid_background);
        TextView mTitle = (TextView) parent.findViewById(R.id.item_grid_title);
        TextView mContent = (TextView) parent.findViewById(R.id.item_grid_content);

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mContainer.getLayoutParams();
        int mwidth = (int) (screenWidth * 0.27);
        params.width = mwidth;
        mContainer.setLayoutParams(params);

        mTitle.setText(model.getVideo_name());
        Date date = new Date(model.getCreate_time() * 1000);
        mContent.setText(DateFormat.getDateInstance().format(date));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBackground.setElevation(4.0f);
        }

        mBackground.setTag(model.getVideo_url());
        mBackground.setOnClickListener(this);
        mBackground.setOnFocusChangeListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view != null) {
            String url = (String) view.getTag();
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("video_url", url);
            startActivity(intent);
        }
    }

    private void showOnFocusAnimation(View v) {
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.button_big);
        v.startAnimation(anim);
        v.bringToFront();
    }

    private void showLoseFocusAnimation(View v) {
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.button_small);
        v.startAnimation(anim);
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (hasFocus) {
            showOnFocusAnimation(view);
        } else {
            showLoseFocusAnimation(view);
        }
    }
}
