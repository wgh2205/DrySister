package com.kok.drysister;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.FileDescriptor;
import java.util.logging.FileHandler;

public class SisterCompress {
    private static final String TAG = "ImageCompress";
    public SisterCompress(){}

    /**
     * 压缩资源图片
     * @param resources
     * @param resId
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public Bitmap decodeBitmapFromResource(Resources resources,int resId,int reqWidth, int reqHeight){
        BitmapFactory.Options options =new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(resources,resId,options);
        //计算缩放比例
        options.inSampleSize = computeSimpleSize(options,reqWidth,reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(resources,resId,options);
    }

    /**
     * 压缩图片文件
     * @param descriptor
     * @param reqWidth
     * @param reqHEight
     * @return
     */
    public Bitmap decodeBitmapFromFileDescriptor(FileDescriptor descriptor,int reqWidth,int reqHEight){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(descriptor,null,options);
        options.inSampleSize = computeSimpleSize(options,reqWidth,reqHEight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFileDescriptor(descriptor,null,options);
    }

    /**
     * 计算压缩比例到方法
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public int computeSimpleSize(BitmapFactory.Options options,int reqWidth,int reqHeight){
        if (reqWidth == 0 || reqHeight == 0){ return  1;}
        int inSampleSize = 1;
        final int height = options.outHeight;
        final int width = options.outWidth;
            Log.v(TAG,"原图大小为："+width+"x"+height);
            if(height >reqHeight || width >reqHeight){
                final int halfWidth = width/2;
                final int halfHeight = height/2;
                while((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth){
                    inSampleSize *=2;
                }
            }
            Log.v(TAG,"inSampleSize =" + inSampleSize );
            return inSampleSize;
        }

}
