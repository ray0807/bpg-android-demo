/*******************************************************************************
 * Copyright 2011-2015 Sergey Tarasevich
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.xmtj.bpgsdkdemo.downloader.universal_image_loader;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import com.nostra13.universalimageloader.core.assist.ContentLengthInputStream;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.xmtj.bpgdecoder.BPG;
import com.xmtj.bpgdecoder.DecoderWrapper;
import com.xmtj.bpgdecoder.constant.Constants;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Implementation of ImageDownloader which uses {@link OkHttpClient} for image stream retrieving.
 *
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 * @author Leo Link (mr[dot]leolink[at]gmail[dot]com)
 */
public class OkHttpImageDownloader extends BaseImageDownloader {

    private OkHttpClient client;
    private Context context;
    private String packageName;
    private String token;

    public OkHttpImageDownloader(Context context, OkHttpClient client) {
        super(context);
        this.client = client;
        this.context = context;
        packageName = context.getPackageName();
        try {
            token = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(),
                            PackageManager.GET_META_DATA).metaData.getString(Constants.METADATE_TAG);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected InputStream getStreamFromNetwork(String imageUri, Object extra) throws IOException {
        String timestamp = System.currentTimeMillis() / 1000 + "";
        RequestBody requestBody = new FormBody.Builder()
                .add("app_name", packageName)
                .add("timestamp", timestamp)
                .add("app_type", "1")
                .add("app_key", BPG.getDecodeString(timestamp, token))
                .add("image", imageUri)
                .build();
        Request request = new Request.Builder().url(Constants.GET_SMALLER_IAMGE_URL).post(requestBody).build();
        Response response = client.newCall(request).execute();

        ResponseBody responseBody = response.body();
        InputStream inputStream = responseBody.byteStream();
        int contentLength = (int) responseBody.contentLength();
        if (Constants.RESOURCE_TAG.equals(response.request().url().host())) {
            //特殊处理
            InputStream stream = null;
            try {
                stream = new ContentLengthInputStream(inputStream, contentLength);
                byte[] decBuffer = DecoderWrapper.decodeBpgBuffer(stream);
                //解码器注册失败重新注册
                if (null == decBuffer) {
                    if (!DecoderWrapper.getInitState()) {
                        BPG.init(context);
                    }
                    //重新请求
                    Request req = new Request.Builder().url(imageUri).get().build();
                    Response res = client.newCall(req).execute();
                    ResponseBody rb = res.body();
                    InputStream is = rb.byteStream();
                    int length = (int) rb.contentLength();
                    return new ContentLengthInputStream(is, length);

                }
                return new ByteArrayInputStream(decBuffer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new ContentLengthInputStream(inputStream, contentLength);
    }
}
