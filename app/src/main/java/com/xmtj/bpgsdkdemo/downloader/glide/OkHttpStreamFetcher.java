package com.xmtj.bpgsdkdemo.downloader.glide;


import android.util.Log;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.GlideUrl;
import com.xmtj.bpgdecoder.DecoderWrapper;
import com.xmtj.bpgdecoder.constant.Constants;
import com.xmtj.bpgsdkdemo.App;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Fetches an {@link InputStream} using the okhttp3library.
 */
public class OkHttpStreamFetcher implements DataFetcher<InputStream> {
    private final Call.Factory client;
    private final GlideUrl url;
    private InputStream stream;
    private ResponseBody responseBody;
    private volatile Call call;

    public OkHttpStreamFetcher(Call.Factory client, GlideUrl url) {
        this.client = client;
        this.url = url;
    }

    @Override
    public InputStream loadData(Priority priority) throws Exception {
       /* RequestBody requestBody = new FormBody.Builder()
                .add("app_name", App.packageName)
                .add("app_key", App.token)
                .add("image", url.toString())
                .build();
        Request request = new Request.Builder().url(Constants.GET_SMALLER_IAMGE_URL).post(requestBody).build();
        Response response = client.newCall(request).execute();

        ResponseBody responseBody = response.body();
        InputStream inputStream = responseBody.byteStream();
        int contentLength = (int) responseBody.contentLength();
        Log.e("wanglei", "response:" + response.headers().toString());
        if (Constants.RESOURCE_TAG.equals(response.headers().get("Content-Type"))) {
            Log.e("wanglei", "开始解码");
            //特殊处理
            InputStream stream = null;
            try {
                stream = new ContentLengthInputStream(inputStream, contentLength);
                byte[] decBuffer = DecoderWrapper.decodeBpgBuffer(stream);
                //解码器注册失败重新注册
                if (null != stream && (null == decBuffer || decBuffer.length == 0)) {
//                    if (!DecoderWrapper.getInitState()) {
//                        BPG.init(context);
//                    }
                    return new ContentLengthInputStream(inputStream, contentLength);
                }
                return new ByteArrayInputStream(decBuffer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new ContentLengthInputStream(inputStream, contentLength);*/

        Request.Builder requestBuilder = new Request.Builder().url(url.toStringUrl());

        for (Map.Entry<String, String> headerEntry : url.getHeaders().entrySet()) {
            String key = headerEntry.getKey();
            requestBuilder.addHeader(key, headerEntry.getValue());
        }
        Request request = requestBuilder.build();

        Response response;
        call = client.newCall(request);
        response = call.execute();
        responseBody = response.body();
        if (!response.isSuccessful()) {
            throw new IOException("Request failed with code: " + response.code());
        }


        long contentLength = responseBody.contentLength();
        stream = com.bumptech.glide.util.ContentLengthInputStream.obtain(responseBody.byteStream(), contentLength);
        Log.e("wanglei", "response:" + response.headers().toString());
        if (Constants.RESOURCE_TAG.equals(response.headers().get("Content-Type"))) {
            Log.e("wanglei", "开始解码");
            //特殊处理
            InputStream decoderStream = null;
            try {
                decoderStream = new ContentLengthInputStream(stream, contentLength);
                byte[] decBuffer = DecoderWrapper.decodeBpgBuffer(decoderStream);
                //解码器注册失败重新注册
                if (null != decoderStream && (null == decBuffer || decBuffer.length == 0)) {
//                    if (!DecoderWrapper.getInitState()) {
//                        BPG.init(context);
//                    }
                    return new ContentLengthInputStream(stream, contentLength);
                }
                return new ByteArrayInputStream(decBuffer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return stream;
    }

    @Override
    public void cleanup() {
        try {
            if (stream != null) {
                stream.close();
            }
        } catch (IOException e) {
            // Ignored
        }
        if (responseBody != null) {
            responseBody.close();
        }
    }

    @Override
    public String getId() {
        return url.getCacheKey();
    }

    @Override
    public void cancel() {
        Call local = call;
        if (local != null) {
            local.cancel();
        }
    }
}
