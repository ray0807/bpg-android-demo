package com.xmtj.bpgsdkdemo.downloader.fresco.config;

import android.content.Context;

import com.facebook.common.internal.Sets;
import com.facebook.common.internal.Supplier;
import com.facebook.imagepipeline.cache.MemoryCacheParams;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.decoder.SimpleProgressiveJpegConfig;
import com.facebook.imagepipeline.listener.RequestListener;
import com.facebook.imagepipeline.listener.RequestLoggingListener;
import com.xmtj.bpgsdkdemo.App;
import com.xmtj.bpgsdkdemo.downloader.OkHttp3ClientManager;
import com.xmtj.bpgsdkdemo.downloader.fresco.OkHttpImagePipelineConfigFactory;

import okhttp3.OkHttpClient;


/**
 * Frescoͼ init config
 */
public class ImagePipelineConfigFactory {

    private static ImagePipelineConfig sImagePipelineConfig;
    private static ImagePipelineConfig sOkHttpImagePipelineConfig;


    public static ImagePipelineConfig getImagePipelineConfig(Context context) {
        if (sImagePipelineConfig == null) {
            ImagePipelineConfig.Builder configBuilder = ImagePipelineConfig.newBuilder(context);
            configBuilder.setProgressiveJpegConfig(new SimpleProgressiveJpegConfig());
            configureCaches(configBuilder);
            configureLoggingListeners(configBuilder);
            sImagePipelineConfig = configBuilder.build();
        }
        return sImagePipelineConfig;
    }


    public static ImagePipelineConfig getOkHttpImagePipelineConfig(Context context) {
        if (sOkHttpImagePipelineConfig == null) {
            ImagePipelineConfig.Builder configBuilder =
                    OkHttpImagePipelineConfigFactory.newBuilder(context, OkHttp3ClientManager.getDefaultClient(context));
//            configBuilder.setProgressiveJpegConfig(new SimpleProgressiveJpegConfig());
//            configureCaches(configBuilder);
            configureLoggingListeners(configBuilder);
            sOkHttpImagePipelineConfig = configBuilder.build();
        }
        return sOkHttpImagePipelineConfig;
    }

    /**
     * Configures disk and memory cache not to exceed common limits
     */
    private static void configureCaches(
            ImagePipelineConfig.Builder configBuilder) {
        final MemoryCacheParams bitmapCacheParams = new MemoryCacheParams(
                1000 * 1024 * 1024, // Max total size of elements in the cache
                400,                     // Max entries in the cache
                1000 * 1024 * 1024, // Max total size of elements in eviction queue
                500,                     // Max length of eviction queue
                Integer.MAX_VALUE);                    // Max cache entry size
        configBuilder
                .setBitmapMemoryCacheParamsSupplier(
                        new Supplier<MemoryCacheParams>() {
                            public MemoryCacheParams get() {
                                return bitmapCacheParams;
                            }
                        })
                /*.setMainDiskCacheConfig(
                        DiskCacheConfig.newBuilder(context)
                                .setBaseDirectoryPath(new File(App.getInstance().getDiskCacheDir()))
                                .setBaseDirectoryName(Constant.IMAGE_CACHE_PATH)
                                .setMaxCacheSize(Constant.MAX_DISK_CACHE_SIZE)
                                .build())*/;
    }

    private static void configureLoggingListeners(ImagePipelineConfig.Builder configBuilder) {
        configBuilder.setRequestListeners(
                Sets.newHashSet((RequestListener) new RequestLoggingListener()));
    }
}
