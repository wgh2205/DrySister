package com.kok.drysister;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.os.StatFs;
import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class DiskCacheHelper {
    private static final String TAG = "DiskCacheHelper";
    private static final long DISK_CACHE_SIZE = 1024*1024*50;//设置磁盘缓冲区大小为50MB
    private static final int DISK_CACHE_INDEX = 0;

    private Context mContext;
    private DiskLruCache mDiskLruCache;
    private SisterCompress mCompress;
    private boolean mIsDiskLruCacheCreated = false;//磁盘缓冲区是否创建

    public DiskCacheHelper(Context mContext) {
        this.mContext = mContext;
        mCompress = new SisterCompress();
        File diskCacheDir = getDiskCacheDir(mContext, "diskCache");
        if (!diskCacheDir.exists()) {
            diskCacheDir.mkdir();
        }
        if (getUsableSpace(diskCacheDir) > DISK_CACHE_SIZE) {
            try{
                mDiskLruCache = DiskLruCache.open(diskCacheDir, 1, 1, DISK_CACHE_SIZE);
                mIsDiskLruCacheCreated = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private File getDiskCacheDir(Context mContext, String diskCacheDirName) {
        //判断机身存储是否可用
        boolean externalStorageAvailable = Environment.getExternalStorageState()
                .equals(Environment.MEDIA_MOUNTED);
        final String cachePath;
        if (externalStorageAvailable) {
            cachePath = mContext.getExternalCacheDir().getPath();
        } else {
            cachePath = mContext.getCacheDir().getPath();
        }
        Log.v(TAG, "硬盘缓存路径：" + cachePath);
        return new File(cachePath + File.separator + diskCacheDirName);
    }

    /**
     * 查询可用空间大小（兼容2.3以下版本）
     * @param path
     * @return
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private Long getUsableSpace(File path) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            return path.getUsableSpace();
        }
        final StatFs statFs = new StatFs(path.getPath());
        return statFs.getBlockSizeLong()*statFs.getAvailableBlocksLong();
    }

    /**
     * 根据Key加载磁盘缓存中的图片
     * @param key
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public Bitmap loadBitmapFromDiskCache(String key, int reqWidth, int reqHeight) throws IOException {
        Log.v(TAG, "加载磁盘缓存中图片");
        //判断是否在主线程里操作
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new RuntimeException("不能在主线程中加载图片");
        }
        if (mDiskLruCache == null) {
            return null;
        }
        Bitmap bitmap = null;
        //获取磁盘缓存中的图片，添加到内存缓存中
        DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
        if (snapshot != null) {
            FileInputStream fileInputStream = (FileInputStream) snapshot.getInputStream(DISK_CACHE_INDEX);
            FileDescriptor fileDescriptor = fileInputStream.getFD();
            bitmap = mCompress.decodeBitmapFromFileDescriptor(fileDescriptor,
                    reqWidth, reqHeight);
        }
        return bitmap;
    }

    /**
     * 将图片字节流缓存到磁盘，并返回一个Bitmap用于显示
     * @param key
     * @param reqWidth
     * @param reqHeight
     * @param bytes
     * @return
     */
    public Bitmap saveImgByte(String key, int reqWidth, int reqHeight, byte[] bytes) {
        //判断是否在主线程里操作
        if (Looper.myLooper() == Looper.getMainLooper()) {
            throw new RuntimeException("不能再主线程中做网络操作!");
        }
        if (mDiskLruCache == null) {
            return null;
        }
        try{
            DiskLruCache.Editor editor = mDiskLruCache.edit(key);
            if (editor != null) {
                OutputStream outputStream = editor.newOutputStream(DISK_CACHE_INDEX);
                outputStream.write(bytes);
                outputStream.flush();
                editor.commit();
                outputStream.close();
                return loadBitmapFromDiskCache(key, reqWidth, reqHeight);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public DiskLruCache getDiskLruCache() {
        return mDiskLruCache;
    }

    public boolean getIsDiskCacheCreate() {
        return mIsDiskLruCacheCreated;
    }

    public void setIsDiskLruCacheCreated(boolean status) {
        this.mIsDiskLruCacheCreated = status;
    }

}
