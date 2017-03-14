package com.xmtj.bpgsdkdemo.adapter;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.xmtj.bpgsdkdemo.R;
import com.xmtj.bpgsdkdemo.contants.Constant;
import com.xmtj.bpgsdkdemo.utils.DisplayUtil;
import com.xmtj.bpgsdkdemo.utils.ViewHolder;


/**
 * @author ray
 * @time 2017-03-14
 * @github https://github.com/ray0807
 * @desc
 */

public class ComicViewerFrescoAdapter extends BaseAdapter {
    private Context context;
    private String datas[];
    private int screenWidth = 0;

    public ComicViewerFrescoAdapter(Context context) {
        this.context = context;
        this.datas = Constant.JPG_VIEWER_IMAGES;

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

            view = LayoutInflater.from(context).inflate(R.layout.item_fresco_viewer, viewGroup, false);
        }
        final SimpleDraweeView sdv_comic_viewer = ViewHolder.get(view, R.id.sdv_comic_viewer);


        final PipelineDraweeControllerBuilder controller = Fresco.newDraweeControllerBuilder();
        ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(Uri.parse(datas[position])).setResizeOptions(new ResizeOptions(2160, 4000)).build();
        controller.setImageRequest(imageRequest);

        controller.setOldController(sdv_comic_viewer.getController());
        controller.setControllerListener(new BaseControllerListener<ImageInfo>() {
            @Override
            public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
                super.onFinalImageSet(id, imageInfo, animatable);
                if (imageInfo == null) {
                    return;
                }
                ViewGroup.LayoutParams params = sdv_comic_viewer.getLayoutParams();
                params.width = DisplayUtil.getScreenWidth(context);
                params.height = DisplayUtil.getScreenWidth(context) * imageInfo.getHeight() / imageInfo.getWidth();
                sdv_comic_viewer.setLayoutParams(params);
            }
        });

        sdv_comic_viewer.setController(controller.build());
        return view;
    }


}
