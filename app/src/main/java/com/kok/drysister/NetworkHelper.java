package com.kok.drysister;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.UrlQuerySanitizer;
import android.os.Message;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HttpsURLConnection;

public class NetworkHelper {
    private static final String TAG = "NetworkHelper";
    private static final int IO_BUFFER_SIZE = 8 *1024;

    /**
     * 根据URL下载图片的方法
     * @param picUrl
     * @return
     */
    public static Bitmap downLoadBitmapFromUrl(String picUrl){
        Bitmap bitmap = null;
        HttpsURLConnection urlConnection = null;
        BufferedInputStream inputStream = null;
        try{
            final URL url=new URL(picUrl);
            urlConnection=(HttpsURLConnection)url.openConnection();
            inputStream=new BufferedInputStream(urlConnection.getInputStream(),IO_BUFFER_SIZE);
            bitmap= BitmapFactory.decodeStream(inputStream);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "下载图片出错："+e );
        }finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            try {
                inputStream.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    /**
     * 根据URL下载图片到数据流
     * @param picUrl
     * @return
     */
    public static  byte[] downLoadUrlToStream(String picUrl){
        InputStream mInputStream;
        ByteArrayOutputStream mOutputStream;
        try{
            URL url=new URL(picUrl);
            HttpURLConnection connection;
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout(10000);
            if (connection.getResponseCode() == 200){
                mInputStream = connection.getInputStream();
                mOutputStream =new ByteArrayOutputStream();
                byte[] bytes = new byte[1024];
                int length;
                while ((length = mInputStream.read(bytes)) != -1){
                    mOutputStream.write(bytes,0,length);
                }
                byte[] results = mOutputStream.toByteArray();

                if (mInputStream != null) {
                    mInputStream.close();
                }
                if (mInputStream != null) {
                    mOutputStream.close();
                }
                return results;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        }catch (Exception e){
            e.printStackTrace();
        }finally {

            }
        return null;
    }

    /**
     * URL转MD5的方法
     * @param url
     * @return
     */
    public static String hashKeyFromUrl(String url){
        String cacheKey = null;

        try{
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(url.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return cacheKey;
    }

    /**
     * 字节数组转MD5到方法
     * @param bytes
     * @return
     */
    public static String bytesToHexString(byte[] bytes){
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                stringBuilder.append('0');
            }
            stringBuilder.append(hex);
        }
        return stringBuilder.toString();
    }
}
