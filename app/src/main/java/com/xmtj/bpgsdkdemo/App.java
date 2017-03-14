package com.xmtj.bpgsdkdemo;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.squareup.leakcanary.LeakCanary;
import com.xmtj.bpgdecoder.BPG;
import com.xmtj.bpgdecoder.constant.Constants;
import com.xmtj.bpgsdkdemo.downloader.OkHttp3ClientManager;
import com.xmtj.bpgsdkdemo.downloader.fresco.config.ImagePipelineConfigFactory;
import com.xmtj.bpgsdkdemo.downloader.universal_image_loader.OkHttpImageDownloader;


/**
 * Created by wanglei on 08/03/17.
 */

public class App extends Application {

    public static String packageName;
    public static String token;

    @Override
    public void onCreate() {
        LeakCanary.install(this);

        init(this);
        //注册解码器
        BPG.init(this);
        //注册universal-image-loader
        initImageLoader(this);
        initFresco(this);
        super.onCreate();
    }

    private void init(Context context) {
        packageName =context.getPackageName();
        try {
            token =context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(),
                            PackageManager.GET_META_DATA).metaData.getString(Constants.METADATE_TAG);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }


    public void initImageLoader(Context context) {

        //监测内存泄漏

        // This configuration tuning is custom. You can tune every option, you may tune some of them,
        // or you can create default configuration by
        //  ImageLoaderConfiguration.createDefault(this);
        // method.


        ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(context);
        config.threadPriority(Thread.NORM_PRIORITY - 2);
        config.denyCacheImageMultipleSizesInMemory();
        config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
        /**
         * it's important
         */
        config.imageDownloader(new OkHttpImageDownloader(context, OkHttp3ClientManager.getDefaultClient(context)));

        config.diskCacheSize(1000 * 1024 * 1024); // 100 MiB
        config.tasksProcessingOrder(QueueProcessingType.FIFO);
        config.writeDebugLogs(); // Remove for release app

        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config.build());
    }

    private void initFresco(Context context) {
        packageName = context.getPackageName();
        try {
            token = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(),
                            PackageManager.GET_META_DATA).metaData.getString(Constants.METADATE_TAG);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Fresco.initialize(context, ImagePipelineConfigFactory.getOkHttpImagePipelineConfig(context));
    }
}
