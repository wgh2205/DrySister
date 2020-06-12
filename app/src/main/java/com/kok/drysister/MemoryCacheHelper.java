package com.kok.drysister;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;

import androidx.core.app.DialogCompat;

public class MemoryCacheHelper {
    private static final String TAG = "MemoryCacheHelper";
    private Context mContext;
    private LruCache<String , Bitmap>mMemoryCache;

    public MemoryCacheHelper(Context mContext) {
        this.mContext = mContext;
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        int cacheSize = maxMemory / 8;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes() * value.getHeight() / 1024;
            }
        };
    }

    /**
     * 获取LruCache对象
     * @return
     */
    public LruCache<String,Bitmap> getmMemoryCache(){
        return mMemoryCache;
    }

    /**
     * 根据key取出lruCache中的Bitmap
     * @param key
     * @return
     */
    public Bitmap getBitmapFromMemoryCache(String key){
        Log.v(TAG,"加载内存中缓存的图片");
        return mMemoryCache.get(key);
    }

    /**
     * 按照key值往LruCache里塞Bitmap
     * @param key
     * @param bitmap
     */
    public void addBitmapToMemoryCache(String key,Bitmap bitmap){
        if (getBitmapFromMemoryCache(key) == null) {
            Log.v(TAG, "将图片缓存到内存中");
            mMemoryCache.put(key, bitmap);
        }
    }
}
