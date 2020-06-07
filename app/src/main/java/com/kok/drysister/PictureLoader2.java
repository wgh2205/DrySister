package com.kok.drysister;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.UrlQuerySanitizer;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
@SuppressLint("HandlerLeak")

public class PictureLoader2 {
    private ImageView loadImg;
    private String imgUrl;
    private byte[] picByte;

    Handler handler2 = new Handler(){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            if (msg.what == 0x123){
                if (picByte != null){
                    Bitmap bitmap = BitmapFactory.decodeByteArray(picByte,0,picByte.length);
                    loadImg.setImageBitmap(bitmap);
                }
            }
        }
    };

    public void load2(ImageView loadImg,String imgUrl){
        this.loadImg = loadImg;
        this.imgUrl = imgUrl;
        Drawable drawable = loadImg.getDrawable();
        if (drawable != null && drawable instanceof BitmapDrawable) {
            Bitmap bitmap =( (BitmapDrawable) drawable).getBitmap();
            if (bitmap != null && !bitmap.isRecycled()){
                bitmap.recycle();
            }
        }
        new Thread(runnable).start();
    }
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
            URL url = new URL(imgUrl);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout(10000);
                if (connection.getResponseCode() == 200){
                    InputStream inputStream = connection.getInputStream();
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    byte[] bytes = new byte[1024];
                    int length = -1;
                    while ((length = inputStream.read(bytes)) != -1){
                        byteArrayOutputStream.write(bytes,0,length);
                    }
                    picByte = byteArrayOutputStream.toByteArray();
                    inputStream.close();
                    byteArrayOutputStream.close();
                    handler2.sendEmptyMessage(0x123);
                }

            }catch (IOException e) {
                e.printStackTrace();
            }
            }
    };
}
