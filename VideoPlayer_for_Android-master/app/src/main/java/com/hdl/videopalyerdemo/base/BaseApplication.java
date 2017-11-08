package com.hdl.videopalyerdemo.base;


import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Context context = this.getApplicationContext();
        //创建默认的ImageLoader配置参数
        ImageLoader.getInstance().init(
                new ImageLoaderConfiguration.Builder(context).
                        defaultDisplayImageOptions(createDefaultBuilder().build())
                        .tasksProcessingOrder(QueueProcessingType.FIFO)
                        .threadPoolSize(6)
                        .threadPriority(Thread.MAX_PRIORITY).build());

    }

    public DisplayImageOptions.Builder createDefaultBuilder() {
        return new DisplayImageOptions.Builder().bitmapConfig(Bitmap.Config.RGB_565)
                .cacheInMemory(true).cacheOnDisk(true).displayer(new FadeInBitmapDisplayer(100));
    }
}
