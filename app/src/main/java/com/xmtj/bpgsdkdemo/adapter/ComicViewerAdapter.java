package com.xmtj.bpgsdkdemo.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.xmtj.bpgsdkdemo.R;
import com.xmtj.bpgsdkdemo.activity.ComicViewAcitivity;
import com.xmtj.bpgsdkdemo.contants.Constant;
import com.xmtj.bpgsdkdemo.downloader.picasso.PicassoUtils;
import com.xmtj.bpgsdkdemo.utils.DisplayUtil;
import com.xmtj.bpgsdkdemo.utils.ViewHolder;


/**
 * Created by wanglei on 08/03/17.
 */

public class ComicViewerAdapter extends BaseAdapter {
    private Context context;
    private String datas[];
    private DisplayImageOptions options;
    private int screenWidth = 0;
    private int type;

    public ComicViewerAdapter(Context context, int type) {
        this.context = context;
        this.type = type;
        this.datas = Constant.JPG_VIEWER_IMAGES;
        switch (type) {
            case ComicViewAcitivity.PICASSO:
                break;
            case ComicViewAcitivity.GLIDE:
                break;
            case ComicViewAcitivity.UNIVERSAL_IMAGE_LOADER:
                options = new DisplayImageOptions.Builder()
                        .cacheInMemory(true)
                        .cacheOnDisk(true)
                        .considerExifParams(true)
                        .build();
                break;
        }

        screenWidth = DisplayUtil.getScreenWidth(context);
    }

    @Override
    public int getCount() {
        return datas.length;
    }

    @Override
    public Object getItem(int i) {
        return datas[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int position, View view, ViewGroup viewGroup) {
        if (view == null) {

            view = LayoutInflater.from(context).inflate(R.layout.item_viewer, viewGroup, false);
        }
        final ImageView iv_comic_viewer = ViewHolder.get(view, R.id.iv_comic_viewer);
        setImage(datas[position], iv_comic_viewer);
        return view;
    }

    private void setImage(String url, final ImageView iv_comic_viewer) {

        switch (type) {
            case ComicViewAcitivity.PICASSO:
                PicassoUtils.displayImageProgress(url, iv_comic_viewer);
                break;
            case ComicViewAcitivity.UNIVERSAL_IMAGE_LOADER:
                ImageLoader.getInstance().displayImage(url, iv_comic_viewer, options, new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String s, View view) {

                    }

                    @Override
                    public void onLoadingFailed(String s, View view, FailReason failReason) {

                    }

                    @Override
                    public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                        try {
                            if (bitmap != null) {
                                ViewGroup.LayoutParams params = iv_comic_viewer.getLayoutParams();
                                params.width = screenWidth;
                                params.height = bitmap.getHeight() * screenWidth / bitmap.getWidth();
                                iv_comic_viewer.setLayoutParams(params);
                            }
                        } catch (Exception e) {

                        }

                    }

                    @Override
                    public void onLoadingCancelled(String s, View view) {

                    }
                });
                break;
            case ComicViewAcitivity.GLIDE:
                Glide.with(context).load(url).into(iv_comic_viewer);
                break;
        }

    }


}
