package com.xmtj.bpgsdkdemo.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.xmtj.bpgsdkdemo.R;
import com.xmtj.bpgsdkdemo.adapter.ComicViewerAdapter;
import com.xmtj.bpgsdkdemo.adapter.ComicViewerFrescoAdapter;
import com.xmtj.bpgsdkdemo.widget.ScrollZoomListView;

/**
 * @author ray
 * @time 2017-03-14
 * @github https://github.com/ray0807
 * @desc
 */
public class ComicViewAcitivity extends Activity {

    public static final String TYPE_TAG = "ComicViewAcitivity";
    public static final int UNIVERSAL_IMAGE_LOADER = 1;
    public static final int FRESCO = 2;
    public static final int PICASSO = 3;
    public static final int GLIDE = 4;

    private ScrollZoomListView rlv_viewer_list;
    private int type;


    public static void start(Activity activity, int type) {
        Intent intent = new Intent(activity, ComicViewAcitivity.class);
        intent.putExtra(TYPE_TAG, type);
        activity.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comic_viewer);
        type = getIntent().getIntExtra(TYPE_TAG, UNIVERSAL_IMAGE_LOADER);


        rlv_viewer_list = (ScrollZoomListView) findViewById(R.id.rlv_viewer_list);
        switch (type) {
            case UNIVERSAL_IMAGE_LOADER:
            case PICASSO:
            case GLIDE:
                rlv_viewer_list.setAdapter(new ComicViewerAdapter(this, type));
                break;
            case FRESCO:
                rlv_viewer_list.setAdapter(new ComicViewerFrescoAdapter(this));
                break;
        }
    }
}
