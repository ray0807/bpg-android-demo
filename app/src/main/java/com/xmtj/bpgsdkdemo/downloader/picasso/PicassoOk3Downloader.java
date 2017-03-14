package com.xmtj.bpgsdkdemo.downloader.picasso;

import android.net.Uri;
import android.support.annotation.IntRange;
import android.support.annotation.WorkerThread;
import android.util.Log;

import com.nostra13.universalimageloader.core.assist.ContentLengthInputStream;
import com.squareup.picasso.Downloader;
import com.squareup.picasso.NetworkPolicy;
import com.xmtj.bpgdecoder.BPG;
import com.xmtj.bpgdecoder.DecoderWrapper;
import com.xmtj.bpgdecoder.constant.Constants;
import com.xmtj.bpgsdkdemo.App;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;
import okio.Source;

/**
 * @author ray
 * @time 2017-03-14
 * @github https://github.com/ray0807
 * @desc
 */

public class PicassoOk3Downloader implements Downloader {
    private final Call.Factory client;
    private final Cache cache;


    public PicassoOk3Downloader(OkHttpClient client, final ProgressListener listener) {
        this.client = client.newBuilder().addNetworkInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                okhttp3.Response originalResponse = chain.proceed(chain.request());
                return originalResponse.newBuilder().body(
                        new PicassoOk3Downloader.ProgressResponseBody(originalResponse.body(), originalResponse.request().url().url().toString(), listener))
                        .build();
            }
        }).build();
        this.cache = ((OkHttpClient) this.client).cache();
    }

    public PicassoOk3Downloader(Call.Factory client) {
        this.client = client;
        this.cache = null;
    }

    public PicassoOk3Downloader(OkHttpClient client) {
        this.client = client;
        this.cache = client.cache();
    }

    /**
     * 如果根据url请求到的不是图片而是字符串的话，支持自动重定向
     *
     * @param uri
     * @param networkPolicy
     * @return
     * @throws IOException
     */
    @Override
    public Response load(Uri uri, int networkPolicy) throws IOException {
        Log.i("wanglei", "准备从php拿url去cdn要图片的uri是->" + uri.toString() + "    缓存策略是" + networkPolicy);
//        CacheControl cacheControl = null;
//        if (networkPolicy != 0) {
//            if (NetworkPolicy.isOfflineOnly(networkPolicy)) {
//                cacheControl = CacheControl.FORCE_CACHE;
//            } else {
//                CacheControl.Builder builder = new CacheControl.Builder();
//                if (!NetworkPolicy.shouldReadFromDiskCache(networkPolicy)) {
//                    builder.noCache();
//                }
//                if (!NetworkPolicy.shouldWriteToDiskCache(networkPolicy)) {
//                    builder.noStore();
//                }
//                cacheControl = builder.build();
//            }
//        }
        // TODO: 14/03/17
        /**
         * picasso缓存策略问题 所以此处使用get请求
         */
        // TODO: 14/03/17
//        RequestBody requestBody = new FormBody.Builder()
//                .add("app_name", App.packageName)
//                .add("app_key", App.token)
//                .add("image", uri.toString())
//                .build();

        Request request = new Request.Builder().cacheControl(new CacheControl.Builder().build()).url(Constants.GET_SMALLER_IAMGE_URL + "?app_name=" + App.packageName + "&app_key=" + App.token + "&image=" + uri.toString()).get().build();
        okhttp3.Response response = client.newCall(request).execute();


        ResponseBody responseBody = response.body();
        InputStream inputStream = responseBody.byteStream();
        int contentLength = (int) responseBody.contentLength();
        if (Constants.RESOURCE_TAG.equals(response.header("Content-Type"))) {
            Log.e("wanglei", "解压特殊图片");
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
                    return new Response(inputStream, response.cacheResponse() != null, contentLength);
                }
                Log.e("wanglei", "解压成功");
                return new Response(new ByteArrayInputStream(decBuffer), response.cacheResponse() != null, decBuffer.length);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
            return new Response(responseBody.byteStream(), true, responseBody.contentLength());
        } else {//如果php发来的不是图片的URL，那就直接用php发来的图片
            boolean fromCache = response.cacheResponse() != null;
            return new Response(responseBody.byteStream(), fromCache, responseBody.contentLength());
        }
    }

    @Override
    public void shutdown() {
        if (cache != null) {
            try {
                cache.close();
            } catch (IOException ignored) {
            }
        }
    }

    public interface ProgressListener {
        @WorkerThread
        void update(@IntRange(from = 0, to = 100) int percent, String url);
    }

    public static class ProgressResponseBody extends ResponseBody {

        private final ResponseBody responseBody;
        private final ProgressListener progressListener;
        private BufferedSource bufferedSource;
        private String url;

        public ProgressResponseBody(ResponseBody responseBody, String url, ProgressListener progressListener) {
            this.responseBody = responseBody;
            this.progressListener = progressListener;
            this.url = url;
            Log.i("Alex", "当前图片是：：" + url);
        }

        @Override
        public MediaType contentType() {
            Log.i("Alex", "contentType是" + responseBody.contentType());
            return responseBody.contentType();
        }

        @Override
        public long contentLength() {
            Log.i("Alex", "contentLength" + responseBody.contentLength());
            return responseBody.contentLength();
        }

        @Override
        public BufferedSource source() {
            if (bufferedSource == null) {
                bufferedSource = Okio.buffer(source(responseBody.source()));
            }
            return bufferedSource;
        }

        private Source source(Source source) {

            return new ForwardingSource(source) {
                long totalBytesRead = 0L;

                @Override
                public long read(Buffer sink, long byteCount) throws IOException {
                    long bytesRead = super.read(sink, byteCount);
                    // read() returns the number of bytes read, or -1 if this source is exhausted.
                    totalBytesRead += bytesRead != -1 ? bytesRead : 0;
                    if (progressListener != null) {
                        progressListener.update(
                                ((int) ((100 * totalBytesRead) / responseBody.contentLength())), url);
                    }
                    return bytesRead;
                }
            };
        }
    }
}
