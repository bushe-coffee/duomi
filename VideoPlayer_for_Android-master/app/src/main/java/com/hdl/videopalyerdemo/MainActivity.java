package com.hdl.videopalyerdemo;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterViewFlipper;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hdl.videopalyerdemo.adapters.ProductFilperAdapter;
import com.hdl.videopalyerdemo.models.AnalysisResultModel;
import com.hdl.videopalyerdemo.models.CommendListModel;
import com.hdl.videopalyerdemo.models.CommendModel;
import com.hdl.videopalyerdemo.models.FacesModel;
import com.hdl.videopalyerdemo.models.ProductModel;
import com.hdl.videopalyerdemo.models.ProductResultModels;
import com.hdl.videopalyerdemo.utils.NetWorkCallback;
import com.hdl.videopalyerdemo.utils.NetWorkUtils;
import com.hdl.videopalyerdemo.utils.YiPlusUtilities;
import com.hdl.vol.OnVedioPalyerListener;
import com.hdl.vol.VedioPlayer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private VedioPlayer vpPlayer;
    //http://bu.dressplus.cn/video/recommand-system/1504193316.mp4
    private String url = "";
    private ProgressDialog mProgressDialog;
    private SeekBar sbProgress;
    private boolean isSeekToed = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sbProgress = (SeekBar) findViewById(R.id.sb_progress);
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
            }

            @Override
            public void onReload() {
            }
        });
        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        sbProgress.setProgress((int) vpPlayer.getCurrentPosition());
                    }
                });
            }
        }, 0, 1000);
    }

    private int curProgress = 0;

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
            screenCapNotRoot();
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (IS_SHOWING_WINDOW) {
                sendMessageForHandle(3, null);
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    // 截图 处理 图像
    private Bitmap screenBitmap;

    private void screenCapNotRoot() {
        if (screenBitmap != null) {
            screenBitmap.recycle();
            screenBitmap = null;
        }

        screenBitmap = vpPlayer.getScreenShot();

        prepareAllData();
        showYiPlusLogo(true);
        screenCapAndRequest();
    }

    public void onPlay(View view) {
        vpPlayer.startPlay();
    }

    public void onPause(View view) {
        vpPlayer.pausePlay();
    }

    // handle data ****************************************************

    private CommendListModel models;
    private static boolean PREPARE_ALL_DATA = false;
    private static boolean PREPARE_IMAGE_BASE64 = false;
    private static boolean IS_SHOWING_WINDOW = false;
    private boolean isFirstPage = true;
    private WindowManager manager;
    private View mContainerView;
    private String mBitmapBase64;
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
                if (!YiPlusUtilities.isStringNullOrEmpty(mBitmapBase64) && PREPARE_ALL_DATA && PREPARE_IMAGE_BASE64) {
                    PREPARE_IMAGE_BASE64 = false;
                    // 任何 图片都会有一个结果。只是返回的是不是 空
                    Product_Face = 0;
                    analysisImage();
                }
            } else if (msg.arg1 == 2) {
                //  set right data and show 识别 列表
                SelectRightResult();
            } else if (msg.arg1 == 3) {
                // close the service
                manager.removeViewImmediate(mContainerView);
                IS_SHOWING_WINDOW = false;
                mContainerView = null;
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
                    updateManagetView(models.getModels(arg2), type, people);
                } else {
                    // click product
                    showProductDetailView();
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

                        sendMessageForHandle(1, null);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void showYiPlusLogo(boolean isOK) {
        if (manager == null) {
            manager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        }

        WindowManager.LayoutParams params = setLayoutParams();
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        View view = getWelcomeViewPage(isOK);
        addViewToManager(view, params);
    }

    private void screenCapAndRequest() {
        mBitmapBase64 = "";
        if (screenBitmap != null) {
            mBitmapBase64 = YiPlusUtilities.BitmapToBase642(screenBitmap);
            PREPARE_IMAGE_BASE64 = true;
            sendMessageForHandle(1, null);
        }
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

    private WindowManager.LayoutParams setLayoutParams() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();

        DisplayMetrics metrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(metrics);
        params.width = metrics.widthPixels / 3;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;

        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        params.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND |
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_SECURE;

        params.gravity = Gravity.RIGHT | Gravity.TOP;
        params.dimAmount = 0.45f;
//        params.alpha = 0.9f;

        return params;
    }

    private View getWelcomeViewPage(boolean isOK) {
        View view = LayoutInflater.from(this).inflate(R.layout.view_welcome_page, null, false);

        ImageButton image = (ImageButton) view.findViewById(R.id.view_welcome_image);
        TextView text = (TextView) view.findViewById(R.id.view_welcome_text);
        if (isOK) {
            image.setImageResource(R.drawable.guangdian_logo);
            text.setText("智能识别请稍后...");
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.animation_scale_big);
            animation.setDuration(1000);
            animation.setRepeatMode(Animation.RESTART);
            animation.setRepeatCount(5);
            image.startAnimation(animation);
        } else {
//            image.setImageResource(R.drawable.result_error);

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

    private void addViewToManager(View view, WindowManager.LayoutParams params) {
        if (manager != null) {
            if (mContainerView != null) {
                manager.removeViewImmediate(mContainerView);
                mContainerView = null;
            }

            view.startAnimation(AnimationUtils.loadAnimation(this, R.anim.animation_join));
            manager.addView(view, params);
            mContainerView = view;
        }
    }

    private void analysisImage() {

        String time = (System.currentTimeMillis() / 1000) + "";
        String data = YiPlusUtilities.getPostParams(time);

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

    private void showProductDetailView() {
        if (manager == null) {
            manager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        }

        WindowManager.LayoutParams params = setLayoutParams();
        View view = getViewForWindowToProduct();

        addViewToManager(view, params);
    }

    private void updateManagetView(List<CommendModel> datas, String source, String people) {
        if (manager == null) {
            manager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        }

        WindowManager.LayoutParams params = setLayoutParams();
        View view = getViewForWindowToDetail(datas, source, people);

        addViewToManager(view, params);
    }

    private void SelectRightResult() {
        clearData();

        // Show analysis result
        if (mAnalysisResultModel != null) {

            Map<String, List<String>> map = handleAnalysisResult();
            if (map == null || map.isEmpty()) {
                // analysis face is null
                Log.d("yang", "没有 识别出来 人 ");
                noAnalysisFaceResult();

                return;
            }

            // match analysis result and all data collections
            List<String> people = map.get("人物");
            List<CommendModel> baidu = models.getModels(0);
            List<CommendModel> dianbo = models.getModels(1);
            List<CommendModel> weibo = models.getModels(2);
            List<CommendModel> taobao = models.getModels(4);

            // people 取第一个识别出来的 人
            String name = (people != null && people.size() > 0) ? people.get(0) : "";
            Log.d("yang", "识别出来的 " + name);
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
                    Log.d("yang", "点播视频 数量 " + dianboPerson.size());
                    mVideo++;
                }

                List<CommendModel> weiboPerson = new ArrayList<>();
                for (CommendModel m : weibo) {
                    if (name.equals(m.getTag_name())) {
                        weiboPerson.add(m);
                    }
                }

                if (weiboPerson.size() > 0) {
                    randomOneData(weiboPerson);
                    mWeibo++;
                }


                List<CommendModel> taobaoPerson = new ArrayList<>();
                for (CommendModel m : taobao) {
                    if (name.equals(m.getTag_name())) {
                        taobaoPerson.add(m);
                    }
                }

                if (taobaoPerson.size() > 0) {
                    randomOneData(taobaoPerson);
                    Log.d("yang", "淘宝 商品 数量 " + taobaoPerson.size());
                    mTaobao++;
                }
            }

            if (mCurrentModels != null && mCurrentModels.size() > 0) {
                hasFace = true;
                // show face list
                if (mCurrentModels != null && mCurrentModels.size() > 1) {
                    showAnsyncList();
                    setListDatas(mCurrentModels);
                } else if (mCurrentModels.size() == 1) {
                    // with only one item and is baidu or dianbo SO, show detail
                    String type = mCurrentModels.get(0).getData_source();
                    String person = mCurrentModels.get(0).getDisplay_title();
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
                        person = mCurrentModels.get(0).getTag_name();
                    }

                    updateManagetView(models.getModels(arg2), type, person);
                }
            } else {
                noAnalysisFaceResult();
            }
        } else {
            noAnalysisFaceResult();
        }
    }

    private View getViewForWindowToProduct() {
        View view = LayoutInflater.from(this).inflate(R.layout.notifi_detail_layout, null, false);

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

        View view = LayoutInflater.from(this).inflate(R.layout.notifi_detail_layout, null, false);

        TextView title = (TextView) view.findViewById(R.id.notifi_page_title);
        mFliper = (AdapterViewFlipper) view.findViewById(R.id.notifi_page_content);
        Button background = (Button) view.findViewById(R.id.notifi_page_background);

//        background.setOnKeyListener(detailKeyListener);

        title.setText(source);
//        FilperAdapter adapter = new FilperAdapter(this);
        if (TAOBAO.equals(source)) {
            List<CommendModel> showTb = new ArrayList<>();
            for (int j = 0; j < datas.size(); ++j) {
                CommendModel tb = datas.get(j);
                if (people.equals(tb.getTag_name())) {
                    showTb.add(tb);
                }
            }

            Log.d("yang", "淘宝 商品  " + showTb.size());
//            adapter.setDatas(showTb, 4);
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

//            adapter.setDatas(showOther, 0);
        }

//        mFliper.setAdapter(adapter);

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

    private Map<String, List<String>> handleAnalysisResult() {
        Map<String, List<String>> map = new HashMap<>();
        FacesModel faces = mAnalysisResultModel.getFaces();

        for (int i = 0; faces != null && i < faces.getFace_counts(); ++i) {
            if (map.containsKey("人物")) {
                map.get("人物").add(faces.getFace_attribute().get(i).getStar_name());
            } else {
                List<String> person = new ArrayList<>();
                person.add(faces.getFace_attribute().get(i).getStar_name());
                map.put("人物", person);
            }
        }

        return map;
    }

    private void noAnalysisFaceResult() {
        hasFace = false;
        if (product_list != null && product_list.size() > 0 && !hasFace) {
            // 展示 商品页面
            showProductDetailView();
        } else {
            // show error page
            showYiPlusLogo(false);
        }
    }

    private void randomOneData(List<CommendModel> datas) {
        Random baiduR = new Random();
        if (datas != null && datas.size() > 0) {
            int random = baiduR.nextInt(datas.size());
            mCurrentModels.add(datas.get(random));
        }

    }

    private void showAnsyncList() {
        if (manager == null) {
            manager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        }

        WindowManager.LayoutParams params = setLayoutParams();
        View view = getViewForWindowToList2();
        addViewToManager(view, params);
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
        View view = LayoutInflater.from(this).inflate(R.layout.view_ansync_list, null, false);
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

        return view;
    }

    private View.OnKeyListener detailKeyListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {

            if (YiPlusUtilities.DOUBLECLICK) {
                System.out.println("majie  button    " + keyCode);
                synchronized (YiPlusUtilities.class) {
                    YiPlusUtilities.DOUBLECLICK = false;
                }

                int id = view.getId();
                if (id == R.id.notifi_page_background) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        manager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
                        WindowManager.LayoutParams params = setLayoutParams();
                        View view2 = getViewForWindowToList2();
                        if (mCurrentModels != null && !isFirstPage) {
                            isFirstPage = true;
                            setListDatas(mCurrentModels);
                            addViewToManager(view2, params);
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

}

