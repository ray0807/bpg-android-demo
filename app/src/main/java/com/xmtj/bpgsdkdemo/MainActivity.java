package com.xmtj.bpgsdkdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.xmtj.bpgdecoder.BPG;
import com.xmtj.bpgsdkdemo.activity.ComicViewAcitivity;

/**
 * @author ray
 * @time 2017-03-14
 * @github https://github.com/ray0807
 * @desc
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_image_downloader).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ComicViewAcitivity.start(MainActivity.this, ComicViewAcitivity.UNIVERSAL_IMAGE_LOADER);
            }
        });
        findViewById(R.id.btn_fresco).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ComicViewAcitivity.start(MainActivity.this, ComicViewAcitivity.FRESCO);
            }
        });
        findViewById(R.id.btn_picasso).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ComicViewAcitivity.start(MainActivity.this, ComicViewAcitivity.PICASSO);
            }
        });
        findViewById(R.id.btn_glide).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ComicViewAcitivity.start(MainActivity.this, ComicViewAcitivity.GLIDE);
            }
        });
    }

    @Override
    protected void onDestroy() {
        BPG.destory();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        ImageLoader.getInstance().stop();
        super.onBackPressed();
    }
}
