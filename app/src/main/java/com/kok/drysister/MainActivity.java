package com.kok.drysister;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btn_next;
    private Button btn_front;
    private ImageView showImg;
    private TextView tv_name;

    private ArrayList<Sister> data;
    private int curPos = 0;
    private int page = 1;
    private PictureLoader loader;
    private SisterApi sisterApi;
    private SisterTask sisterTask;
    private SisterLoader mSisterLoader;
    private SisterDBHelper mSisterDBHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sisterApi =new SisterApi();
        loader = new PictureLoader();
        mSisterLoader = SisterLoader.getInstance(MainActivity.this);
        mSisterDBHelper = SisterDBHelper.getInstance(MainActivity.this);
        initData();
        initUI();
    }

    private void initData() {
        data = new ArrayList<>();
        new SisterTask().execute();
    }

    private void initUI() {
        btn_next = findViewById(R.id.btn_next);
        btn_front = findViewById(R.id.btn_front);
        showImg =  findViewById(R.id.img_show);
        tv_name = findViewById(R.id.tv_name);
        btn_next.setOnClickListener(this);
        btn_front.setOnClickListener(this);
    }

    @Override public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_next:
                btn_front.setVisibility(View.VISIBLE);
                if (curPos < data.size()) {
                    ++curPos;
                }
                if (curPos > data.size() - 1) {
                    sisterTask = new SisterTask();
                    sisterTask.execute();
                } else if (curPos < data.size()) {
                    mSisterLoader.bindBitmap(data.get(curPos).getUrl(), showImg, 400, 400);
                }
                tv_name.setText("第 "+page+"页"+ " | "+"第 "+curPos+"张");
                break;

            case R.id.btn_front:
                --curPos;
                if (curPos == 0) {
                    btn_front.setVisibility(View.INVISIBLE);
                }
                if (curPos == data.size() - 1) {
                    sisterTask = new SisterTask();
                    sisterTask.execute();
                } else if (curPos < data.size()) {
                    mSisterLoader.bindBitmap(data.get(curPos).getUrl(),showImg,400,400);
                }
                tv_name.setText("第 "+page+"页"+ " | "+"第 "+curPos+"张");
                break;

        }
    }

    private class SisterTask extends AsyncTask<Void,Void,ArrayList<Sister>> {

        public SisterTask() {}

        @Override
        protected ArrayList<Sister> doInBackground(Void... voids) {
            ArrayList<Sister> result = new ArrayList<>();
            if (page < (curPos + 1) / 10 + 1) {
                ++page;
            }
            //判断是否有网络
            if (NetworkUtils.isAvailable(getApplicationContext())) {
                result = sisterApi.fetchSister(10, page);
                //查询数据库里有多少妹子数据，避免重复插入
                if (mSisterDBHelper.getSisterCount() / 10 < page) {
                    mSisterDBHelper.insertSisters(result);
                }
            } else {
                result.clear();
                result.addAll(mSisterDBHelper.getSistersLimit(page - 1, 10));
            }
            return result;
        }

        @Override
        protected void onPostExecute(ArrayList<Sister> sisters){
            super.onPostExecute(sisters);
            data.addAll(sisters);
            if (data.size() > 0 && curPos + 1 < data.size()) {
                mSisterLoader.bindBitmap(data.get(curPos).getUrl(),showImg,400,400);
            }
        }

        @Override protected void onCancelled(){
            super.onCancelled();
            sisterTask = null;
        }
    }
    @Override protected void onDestroy(){
        super.onDestroy();
        //sisterTask.cancel(true);
        if (sisterTask != null) {
            sisterTask.cancel(true);
        }
    }
}
