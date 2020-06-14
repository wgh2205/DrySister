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
 * 将网络数据流转换为字节数组再转换为字符串然后还原成json数据集，解析json数据到对应到业务bean链表为前端展示提供Sister链表数据
 * @author KOK 2020/6/7 17:54
 */
public class SisterApi {
    private static final String TAG = "Network";
    private static final String BASE_URL = "https://gank.io/api/data/%E7%A6%8F%E5%88%A9/";
    /**
     * 查询妹子信息,返回一组妹子数据链接
     *
     */
    public ArrayList<Sister> fetchSister(int count,int page){
        ArrayList<Sister> sisters =new ArrayList<>();
        String fetchUrl = BASE_URL+count+"/"+page;
        try{
            URL url =new URL(fetchUrl);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();//通过URL获取接口链接
            connection.setConnectTimeout(5000);
            connection.setRequestMethod("GET");
            int code = connection.getResponseCode();
            Log.v(TAG,"Server response"+code);
            if (code == 200) {
                InputStream inputStream = connection.getInputStream();//通过URL地址打开HTTPURLConnection链接实例，设置参数后获取数据流
                byte[] data = readFromStream(inputStream);//读取数据流中的数据到字节数组中
                String results =new String(data,"UTF-8");//将字节数组转换为字符串
                sisters =parseSister(results);//解析字符串形式的json数据组到业务bean类中再加载到数据链表中
                return sisters;
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
     * 解析字符串形式的json数据集，返回一个妹子数据链表
     * @param content
     * @return
     */
    private ArrayList<Sister> parseSister(String content) throws JSONException {
        ArrayList<Sister> sisters =new ArrayList<>();
        JSONObject object = new JSONObject(content);//根据字符串获取json对象
        JSONArray array = object.getJSONArray("results");//根据指定到key获取json对象中的指定子集
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
     * 读取网络流中的数据到字节数组中
     * @param inputStream
     * @return
     */
    private byte[] readFromStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream =new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) != -1){//将数据流依次读取到buff中直到尾部返回值为-1结束
            outputStream.write(buffer,0,len);//将buffer内容写入输出流中
        }
        inputStream.close();
        return outputStream.toByteArray();//将输出流转化为字节数组
    }
}
