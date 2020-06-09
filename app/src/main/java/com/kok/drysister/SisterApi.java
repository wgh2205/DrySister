package com.kok.drysister;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * 将网络数据流转换为字节数组再转换为字符串然后还原成json数据，然后解析json数据到对应到业务bean链表为前端展示提供图片url
 * @author KOK 2020/6/7 17:54
 */
public class SisterApi {
    private static final String TAG = "Network";
    private static final String BASE_URL = "https://gank.io/api/data/福利/";
    /**
     * 查询妹子信息
     *
     */
    public ArrayList<Sister> fetchSister(int count,int page){
        String fetchUrl = BASE_URL+count+"/"+page;
        ArrayList<Sister> sisters =new ArrayList<>();
        try{
            URL url =new URL(fetchUrl);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setRequestMethod("GET");
            int code = connection.getResponseCode();
            Log.v(TAG,"Server response"+code);
            if (code == 200) {
                InputStream inputStream = connection.getInputStream();
                byte[] data = readFromStream(inputStream);
                String results =new String(data,"UTF-8");
                sisters =parseSister(results);
            }else {
                Log.e(TAG, "请求失败："+code );
            }
        }
         catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return sisters;
    }

    /**
     * 解析json数据
     * @param content
     * @return
     */
    private ArrayList<Sister> parseSister(String content) throws JSONException {
        ArrayList<Sister> sisters =new ArrayList<>();
        JSONObject object = new JSONObject(content);
        JSONArray array = object.getJSONArray("results");
        for (int i=0;i<array.length();i++){
            JSONObject results = (JSONObject) array.get((i));
            Sister sister =new Sister();
            sister.set_id(results.getString("_id"));
            sister.setCreateAt(results.getString("createdAt"));
            sister.setDesc(results.getString("desc"));
            sister.setPublishedAt(results.getString("publishedAt"));
            sister.setSource(results.getString("source"));
            sister.setType(results.getString("type"));
            sister.setUrl(results.getString("url"));
            sister.setUsed(results.getBoolean("used"));
            sister.setWho(results.getString("who"));
            sisters.add(sister);
        }
        return sisters;
    }

    /**
     * 读取网络流中到数据
     * @param inputStream
     * @return
     */
    private byte[] readFromStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream =new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) != -1){
            outputStream.write(buffer,0,len);
        }
        inputStream.close();
        return outputStream.toByteArray();
    }
}
