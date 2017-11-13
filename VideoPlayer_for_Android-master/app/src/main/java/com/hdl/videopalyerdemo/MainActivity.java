package com.hdl.videopalyerdemo;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterViewFlipper;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hdl.videopalyerdemo.adapters.FilperAdapter;
import com.hdl.videopalyerdemo.adapters.ProductFilperAdapter;
import com.hdl.videopalyerdemo.models.AnalysisResultModel;
import com.hdl.videopalyerdemo.models.CommendListModel;
import com.hdl.videopalyerdemo.models.CommendModel;
import com.hdl.videopalyerdemo.models.FacesModel;
import com.hdl.videopalyerdemo.models.PersonModel;
import com.hdl.videopalyerdemo.models.ProductModel;
import com.hdl.videopalyerdemo.models.ProductResultModels;
import com.hdl.videopalyerdemo.utils.NetWorkCallback;
import com.hdl.videopalyerdemo.utils.NetWorkUtils;
import com.hdl.videopalyerdemo.utils.YiPlusUtilities;
import com.hdl.vol.OnVedioPalyerListener;
import com.hdl.vol.VedioPlayer;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private VedioPlayer vpPlayer;
    private String url = "";
    private ProgressDialog mProgressDialog;
    private SeekBar sbProgress;

    private boolean isSeekToed = true;
    private boolean mIsPlaying = false;
    private boolean isContentShow = false;
    // 截图 处理 图像
    private Bitmap screenBitmap;

    private LinearLayout mContent;
    private RelativeLayout mContentParent;
    private Button mContentbg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sbProgress = (SeekBar) findViewById(R.id.sb_progress);
        mContent = (LinearLayout) findViewById(R.id.main_content);
        mContentParent = (RelativeLayout) findViewById(R.id.main_content_parent);
        mContentbg = (Button) findViewById(R.id.main_content_bg);

        sbProgress.setVisibility(View.INVISIBLE);
        sbProgress.setFocusable(false);
        sbProgress.setFocusableInTouchMode(false);

        url = getIntent().getStringExtra("video_url");

        sbProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (!isSeekToed) {
                    vpPlayer.seekTo(seekBar.getProgress());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isSeekToed = false;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isSeekToed = true;
            }
        });

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("加载中...");
        vpPlayer = (VedioPlayer) findViewById(R.id.vp_player);
        vpPlayer.getTextureView().setBufferTimeMax(13.0f);
        vpPlayer.getTextureView().setTimeout(30, 60);
        vpPlayer.play(url, new OnVedioPalyerListener() {
            @Override
            public void onStart() {
                mProgressDialog.show();
            }

            @Override
            public void onPrepare(long total) {
                sbProgress.setMax((int) total);
                mProgressDialog.dismiss();
            }

            @Override
            public void onStartPaly() {
                sbProgress.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(int errorCode, Throwable errorMsg) {
                mProgressDialog.dismiss();
                Toast.makeText(MainActivity.this, "视频url失效", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPuase(long currProgress) {
            }

            @Override
            public void onStop() {
            }

            @Override
            public void onPlayFinished() {
                //Log.e("yang", "onPlayFinished):播放完成了。。。。。。。。");
            }

            @Override
            public void onPlaying(int curBuffPercent) {
                //Log.e("yang", "onPlaying:" + curBuffPercent);
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(vpPlayer != null) {
                                    sbProgress.setProgress((int) vpPlayer.getCurrentPosition());
                                }
                            }
                        });
                    }
                }, 0, 1000);
            }

            @Override
            public void onReload() {
            }
        });

        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        PREPARE_ALL_DATA = false;
        prepareAllData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        vpPlayer.stopPlay();
        vpPlayer = null;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (vpPlayer != null) {
            vpPlayer.runInForeground();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        System.out.println("yangxinyu    " + keyCode);

        if (keyCode == KeyEvent.KEYCODE_MENU) {
            if (screenBitmap != null) {
                screenBitmap.recycle();
                screenBitmap = null;
            }

            if (mIsPlaying) {
                vpPlayer.pausePlay();
                mIsPlaying = false;
            }

            screenCapNotRoot();
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isContentShow) {
                sendMessageForHandle(3, null);
                return true;
            }
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            if (mIsPlaying) {
                vpPlayer.pausePlay();
                mIsPlaying = false;
            } else {
                vpPlayer.startPlay();
                mIsPlaying = true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    private void screenCapNotRoot() {
        screenBitmap = vpPlayer.getScreenShot();

        View loadingView = showYiPlusLogo(true);
        changeLayout(loadingView);

        try {
            if (screenBitmap == null) {
                // TODO  识别失败
                System.out.println("yangxinyu    识别失败 ");
                View errorView = showYiPlusLogo(false);
                changeLayout(errorView);
            } else {
                // TODO show shibie result
                if (PREPARE_ALL_DATA) {
                    System.out.println("yangxinyu    开始 分析截图数据 ");
                    analysisImage();
                } else {
                    Toast.makeText(this, "网络不给力呀...", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // handle data ****************************************************

    private CommendListModel models;
    private static boolean PREPARE_ALL_DATA = false;
    private boolean isFirstPage = true;
    private AnalysisResultModel mAnalysisResultModel;
    private final Object synchronizedObject = new Object();
    private List<ProductModel> product_list = new ArrayList<>();
    private List<CommendModel> mCurrentModels = new ArrayList<>();
    private boolean hasFace = false;

    private AdapterViewFlipper mFliper;

    private int mBaidu = 0;
    private int mWeibo = 0;
    private int mVideo = 0;
    private int mTaobao = 0;

    private static final String BAIDU = "百度百科";
    private static final String WEIBO = "微博";
    private static final String VIDEO = "点播视频";
    private static final String DOUBAN = "豆瓣";
    private static final String TAOBAO = "商品";

    private static int Product_Face = 0; // product and face back ansysics result


    private RelativeLayout baiduContainer;
    private TextView baiduTitle;
    private TextView baiduContent;
    private ImageView baiduImage;
    private Button baiduBg;

    private RelativeLayout dianboContainer;
    private TextView dianboTitle;
    private TextView dianboContent;
    private ImageView dianboImage;
    private Button dianboBg;

    private RelativeLayout weiboContainer;
    private TextView weiboTitle;
    private TextView weiboContent;

    private RelativeLayout taobaoContainer;
    private ImageView taobaoImage;
    private Button taobaoBg;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.arg1 == 1) {

            } else if (msg.arg1 == 2) {
                //  set right data and show 识别 列表
                System.out.println("yangxinyu  selectRightResult  ");
                SelectRightResult();
            } else if (msg.arg1 == 3) {
                if (isContentShow) {
                    isContentShow = false;
                    mContentParent.setVisibility(View.GONE);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT);
                    vpPlayer.setLayoutParams(params);
//                    mVideoView.setFocusable(true);
//                    mVideoView.setFocusableInTouchMode(true);

                    if (!mIsPlaying) {
                        vpPlayer.startPlay();
                        mIsPlaying = true;
                    }
                }
            } else if (msg.arg1 == 4) {
                isFirstPage = false;
                Bundle bundle = msg.getData();
                String tag = bundle.getString("source").trim();
                if (!YiPlusUtilities.isStringNullOrEmpty(tag)) {
                    String source[] = tag.split(" ");
                    String type = source[0];
                    String people = source[1];
                    Log.d("yang", "淘宝   " + type + "   " + people);
                    int arg2 = 0;
                    if (BAIDU.equals(type)) {
                        arg2 = 0;
                    } else if (VIDEO.equals(type)) {
                        arg2 = 1;
                    } else if (WEIBO.equals(type)) {
                        arg2 = 2;
                    } else if (DOUBAN.equals(type)) {
                        arg2 = 3;
                    } else if (TAOBAO.equals(type)) {
                        arg2 = 4;
                    }

                    // show detail view
                    View view = updateManagetView(models.getModels(arg2), type, people);
                    changeLayout(view);
                } else {
                    // click product
                    View view = showProductDetailView();
                    changeLayout(view);
                }
            }
        }
    };

    private void prepareAllData() {
        // TODO get the video all data
        String time = (System.currentTimeMillis() / 1000) + "";
        String data = YiPlusUtilities.getPostParams(time);
        NetWorkUtils.post(YiPlusUtilities.VIDEO_COMMEND_URL, data, null, new NetWorkCallback() {
            @Override
            public void onServerResponse(Bundle result) {
                try {
                    String res = (String) result.get("result");
                    if (!YiPlusUtilities.isStringNullOrEmpty(res)) {
                        JSONArray array = new JSONArray(res);
                        models = new CommendListModel(array);
                        PREPARE_ALL_DATA = true;
                        System.out.println("yangxinyu  prepare data is ok");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void changeLayout(View view) {
        if (mContent != null) {
            mContent.removeAllViews();
            if (!isContentShow) {
                mContentParent.setVisibility(View.VISIBLE);
                mContentbg.setFocusable(true);
                mContentbg.setFocusableInTouchMode(true);
                mContentbg.requestFocus();
                isContentShow = true;
            }

            mContent.addView(view);
        }
    }

    // isOk : true 正在识别 ，false 表示识别错误
    private View showYiPlusLogo(boolean isOK) {
        View view = getWelcomeViewPage(isOK);
        return view;
    }

    private void sendMessageForHandle(int arg, Bundle bundle) {
        synchronized (handler) {
            Message message = new Message();
            message.arg1 = arg;
            if (bundle != null) {
                message.setData(bundle);
            }

            handler.sendMessage(message);
        }
    }

    private View getWelcomeViewPage(boolean isOK) {
        View view = LayoutInflater.from(this).inflate(R.layout.view_welcome_page, mContent, false);

        ImageButton image = (ImageButton) view.findViewById(R.id.view_welcome_image);
        TextView text = (TextView) view.findViewById(R.id.view_welcome_text);
        if (isOK) {
            image.setImageResource(R.drawable.yiplus_logo);
            text.setText("智能识别请稍后...");
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.animation_scale_big);
            animation.setDuration(1000);
            animation.setRepeatMode(Animation.RESTART);
            animation.setRepeatCount(5);
            image.startAnimation(animation);
        } else {
            if (screenBitmap != null) {
                image.setImageBitmap(screenBitmap);
            } else {
                image.setImageResource(R.drawable.result_error);
            }

            text.setText("识别失败，请返回重试...");
        }

        image.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    Log.d("yang", " key Board  " + keyCode);
                    sendMessageForHandle(3, null);
                }
                return false;
            }
        });

        return view;
    }

    private void analysisImage() {

        Product_Face = 0;

        String time = (System.currentTimeMillis() / 1000) + "";
        String data = YiPlusUtilities.getPostParams(time);
        String mBitmapBase64 = YiPlusUtilities.getBase64FromBitmap(screenBitmap);

        String param = null;
        try {
            // base64 得到的 URL 在网络请求过程中 会出现 + 变 空格 的现象。 在 设置 base64 的字符串 之前 进行 格式化
            param = data + "&image=" + URLEncoder.encode(mBitmapBase64, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        // analysis image for getting face
        NetWorkUtils.post(YiPlusUtilities.ANALYSIS_IMAGE_URL, param, null, new NetWorkCallback() {
            @Override
            public void onServerResponse(Bundle result) {
                try {
                    String res = (String) result.get("result");
                    System.out.println("yang  ANALYSIS_IMAGE_URL  " + res);
                    JSONObject object = new JSONObject(res);
                    mAnalysisResultModel = new AnalysisResultModel(object);

                    synchronized (synchronizedObject) {
                        Product_Face++;
                        if (Product_Face == 2) {
                            sendMessageForHandle(2, null);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // analysis image for getting product
        NetWorkUtils.post(YiPlusUtilities.PRODUCT_URL, param, null, new NetWorkCallback() {
            @Override
            public void onServerResponse(Bundle result) {
                try {
                    String res = (String) result.get("result");
                    System.out.println("yang  Product_result   " + res);
                    JSONObject array = new JSONObject(res);

                    ProductResultModels productList = new ProductResultModels(array);
                    product_list.clear();
                    product_list = productList.getProductList();

                    synchronized (synchronizedObject) {
                        Product_Face++;
                        if (Product_Face == 2) {
                            sendMessageForHandle(2, null);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private View showProductDetailView() {
        View view = getViewForWindowToProduct();
        return view;
    }

    private View updateManagetView(List<CommendModel> datas, String source, String people) {
        View view = getViewForWindowToDetail(datas, source, people);
        return view;
    }

    private void SelectRightResult() {
        clearData();

        String name;
        // Show analysis result
        if (mAnalysisResultModel != null) {

            FacesModel faces = mAnalysisResultModel.getFaces();
            // get the first item as the person
            if (faces != null) {
                List<PersonModel> personModels = faces.getFace_attribute();
                if (personModels != null && !YiPlusUtilities.isListNullOrEmpty(personModels)) {
                    name = personModels.get(0).getStar_name();
                    Log.d("yangxinyu ", "识别出来的 " + name);
                    // match analysis result and all data collections
                    List<CommendModel> baidu = models.getModels(0);
                    List<CommendModel> dianbo = models.getModels(1);
                    List<CommendModel> taobao = models.getModels(4);

                    // get some data about the person
                    if (!YiPlusUtilities.isStringNullOrEmpty(name)) {
                        for (CommendModel m : baidu) {
                            if (name.equals(m.getTag_name()) && mBaidu == 0) {
                                mCurrentModels.add(m);
                                mBaidu++;
                                break;
                            }
                        }

                        List<CommendModel> dianboPerson = new ArrayList<>();
                        for (CommendModel m : dianbo) {
                            if (name.equals(m.getTag_name())) {
                                dianboPerson.add(m);
                            }
                        }

                        // 把 所有的 该人物的点播视频 识别出来。 在随机一个 放到 mCurrentModels
                        if (dianboPerson.size() > 0) {
                            randomOneData(dianboPerson);
                            Log.d("yangxinyu+", "点播视频 数量 " + dianboPerson.size());
                            mVideo++;
                        }

                        List<CommendModel> taobaoPerson = new ArrayList<>();
                        for (CommendModel m : taobao) {
                            if (name.equals(m.getTag_name())) {
                                taobaoPerson.add(m);
                            }
                        }

                        if (taobaoPerson.size() > 0) {
                            randomOneData(taobaoPerson);
                            Log.d("yangxinyu+", "淘宝 商品 数量 " + taobaoPerson.size());
                            mTaobao++;
                        }
                    }

                    //分析这个人的数据
                    if (mCurrentModels != null && mCurrentModels.size() > 0) {
                        //hasFace = true;
                        // show face list
                        if (mCurrentModels.size() > 1) {
                            // show some item data about the person
                            System.out.println("yangxinyu    展示 list 数据 ");
                            View view = getViewForWindowToList2();
                            changeLayout(view);
                            return;
                        } else if (mCurrentModels.size() == 1) {
                            // with only one item and is baidu , show detail
                            String type = mCurrentModels.get(0).getData_source();
                            int arg2 = 0;
                            if (BAIDU.equals(type)) {
                                arg2 = 0;
                                // show baidu baike data
                                System.out.println("yangxinyu    展示 百度百科");
                                View view = getViewForWindowToDetail(models.getModels(arg2), type, name);
                                changeLayout(view);
                                return;
                            } else {
                                Toast.makeText(this, "别看他了， 他低调...", Toast.LENGTH_SHORT).show();
                                View errorView = getWelcomeViewPage(false);
                                changeLayout(errorView);
                                return;
                            }
                        }
                    }
                }
            }
        }

        noAnalysisFaceResult();
    }

    private View getViewForWindowToProduct() {
        View view = LayoutInflater.from(this).inflate(R.layout.notifi_detail_layout, mContent, false);

        TextView title = (TextView) view.findViewById(R.id.notifi_page_title);
        mFliper = (AdapterViewFlipper) view.findViewById(R.id.notifi_page_content);
        Button background = (Button) view.findViewById(R.id.notifi_page_background);

        background.setOnKeyListener(detailKeyListener);

        title.setText("商品");

        ProductFilperAdapter adapter = new ProductFilperAdapter(this);
        adapter.setDatas(product_list);

        mFliper.setAdapter(adapter);

        return view;
    }

    private View getViewForWindowToDetail(List<CommendModel> datas, String source, String people) {

        View view = LayoutInflater.from(this).inflate(R.layout.notifi_detail_layout, mContent, false);

        TextView title = (TextView) view.findViewById(R.id.notifi_page_title);
        mFliper = (AdapterViewFlipper) view.findViewById(R.id.notifi_page_content);
        Button background = (Button) view.findViewById(R.id.notifi_page_background);

        background.setOnKeyListener(detailKeyListener);

        title.setText(source);
        FilperAdapter adapter = new FilperAdapter(this);
        if (TAOBAO.equals(source)) {
            List<CommendModel> showTb = new ArrayList<>();
            for (int j = 0; j < datas.size(); ++j) {
                CommendModel tb = datas.get(j);
                if (people.equals(tb.getTag_name())) {
                    showTb.add(tb);
                }
            }

            Log.d("yang", "淘宝 商品  " + showTb.size());
            adapter.setDatas(showTb, 4);
        } else {
            List<CommendModel> showOther = new ArrayList<>();
            for (int i = 0; i < datas.size(); ++i) {
                CommendModel model = datas.get(i);
                if (people.equals(model.getDisplay_title())) {
                    if (BAIDU.equals(source)) {
                        showOther.clear();
                        showOther.add(model);
                    } else {
                        showOther.add(model);
                    }

                    break;
                }
            }

            adapter.setDatas(showOther, 0);
        }

        mFliper.setAdapter(adapter);

        return view;
    }

    private void clearData() {
        if (mCurrentModels != null) {
            mCurrentModels.clear();
        }

        mBaidu = 0;
        mWeibo = 0;
        mVideo = 0;
        mTaobao = 0;
    }

    private void noAnalysisFaceResult() {
        hasFace = false;
        if (product_list != null && product_list.size() > 0 && !hasFace) {
            // 展示 商品页面
            System.out.println("yangxinyu  没有识别出 人来，展示产品页面");
            View productView = showProductDetailView();
            changeLayout(productView);
        } else {
            // show error page
            View view = showYiPlusLogo(false);
            changeLayout(view);
        }
    }

    private void randomOneData(List<CommendModel> datas) {
        Random baiduR = new Random();
        if (datas != null && datas.size() > 0) {
            int random = baiduR.nextInt(datas.size());
            mCurrentModels.add(datas.get(random));
        }

    }

    private View showAnsyncList() {
        View view = getViewForWindowToList2();
        return view;
    }

    private void setListDatas(List<CommendModel> listDatas) {
        for (CommendModel model : listDatas) {
            if (BAIDU.equals(model.getData_source())) {
                baiduTitle.setText(model.getDisplay_title());
                baiduContent.setText(model.getDisplay_brief());
//                ImageLoader.getInstance().displayImage(model.getDetailed_image_url(), baiduImage);
                baiduBg.setTag(model.getData_source() + " " + model.getDisplay_title());
            } else if (VIDEO.equals(model.getData_source())) {
                dianboTitle.setText(model.getDisplay_title());
                dianboContent.setText(model.getDisplay_brief());
//                ImageLoader.getInstance().displayImage(model.getDetailed_image_url(), dianboImage);
                dianboBg.setTag(model.getData_source() + " " + model.getDisplay_title());
            } else if (WEIBO.equals(model.getData_source())) {
                if (YiPlusUtilities.isStringNullOrEmpty(model.getDisplay_title()) || "null".equals(model.getDisplay_title())) {
                    weiboTitle.setVisibility(View.GONE);
                } else {
                    weiboTitle.setText(model.getDisplay_title());
                }

                if (YiPlusUtilities.isStringNullOrEmpty(model.getDisplay_brief()) || "null".equals(model.getDisplay_brief())) {
                    weiboContent.setVisibility(View.GONE);
                } else {
                    weiboContent.setText(model.getDisplay_brief());
                }
            } else if (TAOBAO.equals(model.getData_source()) && hasFace) {
                Log.d("yang", "image URL  " + model.getDetailed_image_url());
//                ImageLoader.getInstance().displayImage(model.getDetailed_image_url(), taobaoImage);
                taobaoBg.setTag(model.getData_source() + " " + model.getTag_name());
            }
        }
    }

    private View getViewForWindowToList2() {
        View view = LayoutInflater.from(this).inflate(R.layout.view_ansync_list, mContent, false);
        baiduContainer = (RelativeLayout) view.findViewById(R.id.view_baidu_container);
        baiduTitle = (TextView) view.findViewById(R.id.view_baidu_title);
        baiduContent = (TextView) view.findViewById(R.id.view_baidu_content);
        baiduImage = (ImageView) view.findViewById(R.id.view_baidu_image);
        baiduBg = (Button) view.findViewById(R.id.view_baidu_button);

        dianboContainer = (RelativeLayout) view.findViewById(R.id.view_dianbo_container);
        dianboTitle = (TextView) view.findViewById(R.id.view_dianbo_title);
        dianboContent = (TextView) view.findViewById(R.id.view_dianbo_content);
        dianboImage = (ImageView) view.findViewById(R.id.view_dianbo_image);
        dianboBg = (Button) view.findViewById(R.id.view_dianbo_button);

        weiboContainer = (RelativeLayout) view.findViewById(R.id.view_weibo_container);
        weiboTitle = (TextView) view.findViewById(R.id.view_weibo_title);
        weiboContent = (TextView) view.findViewById(R.id.view_weibo_content);

        taobaoContainer = (RelativeLayout) view.findViewById(R.id.view_taobao_container);
        taobaoImage = (ImageView) view.findViewById(R.id.view_taobao_image);
        taobaoBg = (Button) view.findViewById(R.id.view_taobao_button);

        baiduContainer.setVisibility(mBaidu == 0 ? View.GONE : View.VISIBLE);
        dianboContainer.setVisibility(mVideo == 0 ? View.GONE : View.VISIBLE);
        weiboContainer.setVisibility(mWeibo == 0 ? View.GONE : View.VISIBLE);
        taobaoContainer.setVisibility(mTaobao == 0 ? View.GONE : View.VISIBLE);

//        baiduBg.setOnKeyListener(listKeyListener);
//        dianboBg.setOnKeyListener(listKeyListener);
//        taobaoBg.setOnKeyListener(listKeyListener);

        ImageButton image = (ImageButton) view.findViewById(R.id.view_screen_cap);

        if (screenBitmap != null) {
            image.setImageBitmap(screenBitmap);
        }

        for (CommendModel model : mCurrentModels) {
            if (BAIDU.equals(model.getData_source())) {
                baiduTitle.setText(model.getDisplay_title());
                baiduContent.setText(model.getDisplay_brief());
                ImageLoader.getInstance().displayImage(model.getDetailed_image_url(), baiduImage);
                baiduBg.setTag(model.getData_source() + " " + model.getDisplay_title());
            } else if (VIDEO.equals(model.getData_source())) {
                dianboTitle.setText(model.getDisplay_title());
                dianboContent.setText(model.getDisplay_brief());
                ImageLoader.getInstance().displayImage(model.getDetailed_image_url(), dianboImage);
                dianboBg.setTag(model.getData_source() + " " + model.getDisplay_title());
            } else if (TAOBAO.equals(model.getData_source())) {
                Log.d("Yi+", "image URL  " + model.getDetailed_image_url());
                ImageLoader.getInstance().displayImage(model.getDetailed_image_url(), taobaoImage);
                taobaoBg.setTag(model.getData_source() + " " + model.getTag_name());
            }
        }

        return view;
    }

    private View.OnKeyListener detailKeyListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {

            if (YiPlusUtilities.DOUBLECLICK) {
                System.out.println("yangxinyu  button    " + keyCode);
                synchronized (YiPlusUtilities.class) {
                    YiPlusUtilities.DOUBLECLICK = false;
                }

                int id = view.getId();
                if (id == R.id.notifi_page_background) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        View view2 = getViewForWindowToList2();
                        if (mCurrentModels != null && !isFirstPage) {
                            isFirstPage = true;
                            setListDatas(mCurrentModels);
                            changeLayout(view2);
                        } else {
                            sendMessageForHandle(3, null);
                        }

                        return true;
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                        if (mFliper != null) {
                            mFliper.showNext();
                        }
                        return true;
                    } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                        if (mFliper != null) {
                            mFliper.showPrevious();
                        }
                        return true;
                    } else if ((keyCode == KeyEvent.KEYCODE_DPAD_CENTER)) {
                        return true;
                    }
                }
            }

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    synchronized (YiPlusUtilities.class) {
                        YiPlusUtilities.DOUBLECLICK = true;
                    }
                }
            }, 1000);

            return false;
        }
    };

    @Override
    public void onClick(View v) {

    }
}

