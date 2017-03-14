package com.xmtj.bpgsdkdemo.downloader.picasso;

import android.content.Context;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.xmtj.bpgsdkdemo.downloader.OkHttp3ClientManager;


import okhttp3.OkHttpClient;

/**
 * @author ray
 * @time 2017-03-14
 * @github https://github.com/ray0807
 * @desc
 */

public class PicassoUtils {
    private static Picasso picasso;

    /**
     * 获得单例的Picasso，如果不单例那么Lru缓存就会失效
     *
     * @param context
     * @return
     */
    static private Picasso getPicasso(Context context) {
        OkHttpClient client = OkHttp3ClientManager.getDefaultClient(context);
        PicassoOk3Downloader downloader = new PicassoOk3Downloader(client);
        if (picasso == null) picasso = new Picasso.Builder(context).downloader(downloader).build();
        return picasso;
    }

    public static void displayImageProgress(final String url, ImageView imageView) {
        if (picasso == null)
            getPicasso(imageView.getContext());
        picasso.load(url).into(imageView);//模糊图占位
    }
}
