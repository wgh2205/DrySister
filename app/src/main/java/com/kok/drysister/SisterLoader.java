package com.kok.drysister;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.LogRecord;

import androidx.annotation.NonNull;

/**
 * 图片加载的主要控制类
 */

public class SisterLoader {
    private static final String TAG = "SisterLoader";

    private static final int MESSAGE_POST_RESULT = 1;
    private static final int TAG_KEY_URI=R.id.sister_loader_uri;//一个常量值，setTag用到

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();//获取CPU个数
    private static final int CORE_POOL_SIZE = CPU_COUNT +1; //核心线程数
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1; //最大线程池大小
    private static final long KEEP_ALIVE = 10L; // 线程空闲时间；

    private Context mContext;
    private MemoryCacheHelper mMemoryCacheHelper;
    private DiskCacheHelper mDiskCacheHelper;

    /**
     * 线程工程创建线程
     */
    private static final ThreadFactory mFactory =new ThreadFactory() {
        private final AtomicInteger mAtomicInteger = new AtomicInteger(1);
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "SisterLoader#" + mAtomicInteger.getAndIncrement());
        }
    };
    //线程池管理线程
    public static final Executor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(
            CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS,
            new LinkedBlockingDeque<Runnable>(), mFactory);

    private Handler mMainHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            LoaderResult result = (LoaderResult) msg.obj;
            ImageView resultImage = result.imageView;
            //设置图片大小，加载图片
            ViewGroup.LayoutParams params = resultImage.getLayoutParams();
            params.width = SizeUtils.dp2px(mContext.getApplicationContext(),result.reqWidth);
            params.height = SizeUtils.dp2px(mContext.getApplicationContext(), result.reqHeight);
            resultImage.setLayoutParams(params);
            //resultImage.setImageBitmap(result.bitmap);
            String uri = (String) resultImage.getTag(TAG_KEY_URI);
            if (uri.equals(result.uri)) {
                resultImage.setImageBitmap(result.bitmap);
            } else {
                Log.w(TAG, "URL发生改变，不设置图片" );
            }
        }
    };

    private SisterLoader(Context context) {
        mContext = context.getApplicationContext();
        mMemoryCacheHelper = new MemoryCacheHelper(mContext);
        mDiskCacheHelper = new DiskCacheHelper(mContext);
    }

    public static SisterLoader getInstance(Context context) {
        return new SisterLoader(context);
    }

    /**
     * 同步加载图片，该方法只能在主线程执行
     * @param url
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    private Bitmap loadBitmap(String url,int reqWidth,int reqHeight){
        final String key = NetworkHelper.hashKeyFromUrl(url);
        //1.先到内存缓存中找
        Bitmap bitmap = mMemoryCacheHelper.getBitmapFromMemoryCache(key);
        if (bitmap != null) {
            Log.v(TAG, "在内存中找到缓存图片");
            return bitmap;
        }
        //2.到磁盘缓存中找
        try {
            bitmap = mDiskCacheHelper.loadBitmapFromDiskCache(key, reqWidth, reqHeight);
            //如果磁盘缓存中找到，往内存缓存中存一份
            if (bitmap != null) {
                Log.v(TAG, "在硬盘中找到缓存图片");
                mMemoryCacheHelper.addBitmapToMemoryCache(key,bitmap);
                return bitmap;
            }
            //3.磁盘中找不到，加载网络图片
            if (NetworkUtils.isAvailable(mContext)) {
                Log.d(TAG, "加载网络上的图片，URL：" + url);
                byte[] bytes= NetworkHelper.downLoadUrlToStream(url);
                //如果网络数据加载成功往硬盘缓存一份
                bitmap = mDiskCacheHelper.saveImgByte(key, reqWidth, reqHeight, NetworkHelper.downLoadUrlToStream(url));

                /*                if ( bytes != null) {
                    //如果网络数据加载成功往硬盘缓存一份
                    bitmap = mDiskCacheHelper.saveImgByte(key, reqWidth, reqHeight, NetworkHelper.downLoadUrlToStream(url));
                } */

                /*else {
                    Log.d(TAG, "加载网络上的图片失败！！！");
                    //Resources resources = mContext.getResources();
                    // bitmap = BitmapFactory.decodeResource(resources,R.mipmap.miss2333);
                    // BitmapDrawable bitmapDrawable = (BitmapDrawable) mContext.getResources().getDrawable(R.drawable.miss2333);;
                    //   bitmap = mDiskCacheHelper.saveImgByte(key2, reqWidth, reqHeight,SizeUtils.image2Bytes(resources.getDrawable(R.raw.miss2333, null)));
                    String URL_DEFAULT = R.string.defaultUrl;
                    String key2 = NetworkHelper.hashKeyFromUrl(URL_DEFAULT);
                    bitmap = mDiskCacheHelper.saveImgByte(key2, reqWidth, reqHeight, NetworkHelper.downLoadUrlToStream(URL_DEFAULT));
                }*/
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (bitmap == null && !mDiskCacheHelper.getIsDiskCacheCreate()) {
            Log.w(TAG, "磁盘缓存创建失败！！！" );
            bitmap = NetworkHelper.downLoadBitmapFromUrl(url);
        }
        return bitmap;
    }

    /**
     * 异步加载图片
     * @param url
     * @param imageView
     * @param reqWidth
     * @param reqHeight
     */
    public void bindBitmap(final String url, final ImageView imageView, final int reqWidth, final int reqHeight) {
        imageView.setTag(TAG_KEY_URI, url);
        final Runnable loadBitmapTask = new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = loadBitmap(url, reqWidth, reqHeight);
                if (bitmap != null) {
                    LoaderResult result = new LoaderResult(imageView, url, bitmap, reqWidth, reqHeight);
                    mMainHandler.obtainMessage(MESSAGE_POST_RESULT,result).sendToTarget();
                }
            }
        };
        THREAD_POOL_EXECUTOR.execute(loadBitmapTask);
    }
}
