package com.xmtj.bpgsdkdemo.downloader.fresco;

import android.net.Uri;
import android.os.SystemClock;
import android.util.Log;

import com.facebook.common.logging.FLog;
import com.facebook.imagepipeline.image.EncodedImage;
import com.facebook.imagepipeline.producers.BaseNetworkFetcher;
import com.facebook.imagepipeline.producers.Consumer;
import com.facebook.imagepipeline.producers.FetchState;
import com.facebook.imagepipeline.producers.ProducerContext;
import com.nostra13.universalimageloader.core.assist.ContentLengthInputStream;
import com.xmtj.bpgdecoder.BPG;
import com.xmtj.bpgdecoder.DecoderWrapper;
import com.xmtj.bpgdecoder.constant.Constants;
import com.xmtj.bpgsdkdemo.App;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * @author ray
 * @time 2017-03-14
 * @github https://github.com/ray0807
 * @desc
 */

public class OkHttpNetworkFetcher extends
        BaseNetworkFetcher<OkHttpNetworkFetcher.OkHttpNetworkFetchState> {

    public static class OkHttpNetworkFetchState extends FetchState {
        public long submitTime;
        public long responseTime;
        public long fetchCompleteTime;

        public OkHttpNetworkFetchState(
                Consumer<EncodedImage> consumer,
                ProducerContext producerContext) {
            super(consumer, producerContext);
        }
    }

    private static final String TAG = "OkHttpNetworkFetchProducer";
    private static final String QUEUE_TIME = "queue_time";
    private static final String FETCH_TIME = "fetch_time";
    private static final String TOTAL_TIME = "total_time";
    private static final String IMAGE_SIZE = "image_size";

    private final Call.Factory mCallFactory;
    private OkHttpClient client;

    private Executor mCancellationExecutor;


    /**
     * @param okHttpClient client to use
     */
    public OkHttpNetworkFetcher(OkHttpClient okHttpClient) {
        this(okHttpClient, okHttpClient.dispatcher().executorService());
        this.client = okHttpClient;
    }

    /**
     * @param callFactory          custom {@link Call.Factory} for fetching image from the network
     * @param cancellationExecutor executor on which fetching cancellation is performed if
     *                             cancellation is requested from the UI Thread
     */
    public OkHttpNetworkFetcher(Call.Factory callFactory, Executor cancellationExecutor) {
        mCallFactory = callFactory;
        mCancellationExecutor = cancellationExecutor;
    }

    @Override
    public OkHttpNetworkFetchState createFetchState(
            Consumer<EncodedImage> consumer,
            ProducerContext context) {
        return new OkHttpNetworkFetchState(consumer, context);
    }

    @Override
    public void fetch(final OkHttpNetworkFetchState fetchState, final Callback callback) {
        fetchState.submitTime = SystemClock.elapsedRealtime();
        final Uri uri = fetchState.getUri();

        try {

//            RequestBody requestBody = new FormBody.Builder()
//                    .add("app_name", App.packageName)
//                    .add("app_key", App.token)
//                    .add("image", uri.toString())
//                    .build();
            String timestamp = System.currentTimeMillis() / 1000 + "";
            Request request = new Request.Builder()
                    .cacheControl(new CacheControl.Builder().build())
//                    .url(Constants.GET_SMALLER_IAMGE_URL)
//                    .post(requestBody)
                    .url(Constants.GET_SMALLER_IAMGE_URL + "?app_name=" + App.packageName + "&app_key=" + BPG.getDecodeString(timestamp, App.token) + "&image=" + uri.toString() + "&timestamp=" + timestamp + "&app_type=1")
                    .get()
                    .build();

//            Request request = new Request.Builder()
//                    .cacheControl(new CacheControl.Builder().noStore().build())
//                    .url(uri.toString())
//                    .get()
//                    .build();


            fetchWithRequest(fetchState, callback, request);
        } catch (Exception e) {
            e.printStackTrace();
            // handle error while creating the request
            callback.onFailure(e);
        }
    }

    @Override
    public void onFetchCompletion(OkHttpNetworkFetchState fetchState, int byteSize) {
        fetchState.fetchCompleteTime = SystemClock.elapsedRealtime();
    }

    @Override
    public Map<String, String> getExtraMap(OkHttpNetworkFetchState fetchState, int byteSize) {
        Map<String, String> extraMap = new HashMap<>(4);
        extraMap.put(QUEUE_TIME, Long.toString(fetchState.responseTime - fetchState.submitTime));
        extraMap.put(FETCH_TIME, Long.toString(fetchState.fetchCompleteTime - fetchState.responseTime));
        extraMap.put(TOTAL_TIME, Long.toString(fetchState.fetchCompleteTime - fetchState.submitTime));
        extraMap.put(IMAGE_SIZE, Integer.toString(byteSize));
        return extraMap;
    }

    protected void fetchWithRequest(
            final OkHttpNetworkFetchState fetchState,
            final Callback callback,
            final Request request) {
        try {
            final Call call = client.newCall(request);
//            fetchState.getContext().addCallbacks(
//                    new BaseProducerContextCallbacks() {
//                        @Override
//                        public void onCancellationRequested() {
//                            if (Looper.myLooper() != Looper.getMainLooper()) {
//                                call.cancel();
//                            } else {
//                                mCancellationExecutor.execute(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        call.cancel();
//                                    }
//                                });
//                            }
//                        }
//                    });


            Response response = call.execute();
            fetchState.responseTime = SystemClock.elapsedRealtime();
            final ResponseBody body = response.body();
            try {
//                if (!response.isSuccessful()) {
//                    handleException(
//                            call,
//                            new IOException("Unexpected HTTP code " + response),
//                            callback);
//                    return;
//                }

                long contentLength = body.contentLength();
                if (contentLength < 0) {
                    contentLength = 0;
                }
                if (Constants.RESOURCE_TAG.equals(response.request().url().host())) {
                    //特殊处理
                    InputStream stream = null;
                    byte[] decBuffer = null;
                    try {
                        stream = new ContentLengthInputStream(body.byteStream(), (int) contentLength);
                        decBuffer = DecoderWrapper.decodeBpgBuffer(stream);
                        //解码器注册失败重新注册
                        if (null != stream && null == decBuffer) {
//                                        if (!DecoderWrapper.getInitState()) {
//                                            BPG.init(context);
//                                        }
                            callback.onFailure(new Throwable("图片解析失败"));
                            return;
                        }
                        callback.onResponse(new ByteArrayInputStream(decBuffer), decBuffer.length);
                    } catch (Exception e) {
                        e.printStackTrace();
                        callback.onFailure(new Throwable("图片解析失败"));
                    } finally {
                        if (null != stream) {
                            stream.close();
                            stream = null;
                        }
                        if (null != decBuffer) {
                            decBuffer = null;
                        }

                    }
                } else {
                    callback.onResponse(body.byteStream(), (int) contentLength);
                }


            } catch (Exception e) {
                e.printStackTrace();
                handleException(call, e, callback);
            } finally {
                try {
                    body.close();
                } catch (Exception e) {
                    FLog.w(TAG, "Exception when closing response body", e);
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    /**
     * Handles exceptions.
     * <p>
     * <p> OkHttp notifies callers of cancellations via an IOException. If IOException is caught
     * after request cancellation, then the exception is interpreted as successful cancellation
     * and onCancellation is called. Otherwise onFailure is called.
     */
    private void handleException(final Call call, final Exception e, final Callback callback) {
        if (call.isCanceled()) {
            callback.onCancellation();
        } else {
            callback.onFailure(e);
        }
    }
}
