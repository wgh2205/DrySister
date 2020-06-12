package com.kok.drysister;

import android.graphics.Bitmap;
import android.widget.ImageView;

/**
 * 加载结果类
 */
public class LoaderResult {
    public ImageView imageView;
    public String uri;
    public Bitmap bitmap;
    public int reqWidth;
    public int reqHeight;

    public LoaderResult(ImageView imageView, String uri, Bitmap bitmap, int reqWidth, int reqHeight) {
        this.imageView = imageView;
        this.uri = uri;
        this.bitmap = bitmap;
        this.reqWidth = reqWidth;
        this.reqHeight = reqHeight;
    }
}
