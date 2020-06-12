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

    private Button showBtn;
    private Button refreshBtn;
    private ImageView showImg;
    private TextView tv_name;

    private ArrayList<Sister> data;
    private int curPos = 0;
    private int page = 1;
    private  PictureLoader loader;
    private SisterApi sisterApi;
    private SisterTask sisterTask;
    private ArrayList<String> urls;

    private SisterLoader mSisterLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sisterApi =new SisterApi();
        loader = new PictureLoader();
        mSisterLoader = SisterLoader.getInstance(MainActivity.this);
        initData();
        initUI();
    }

    private void initData() {
        data = new ArrayList<>();
        new SisterTask(page).execute();
    }

    private void initUI() {
        showBtn = findViewById(R.id.btn_show);
        refreshBtn = findViewById(R.id.btn_refresh);
        showImg =  findViewById(R.id.img_show);
        tv_name = findViewById(R.id.tv_name);
        showBtn.setOnClickListener(this);
        refreshBtn.setOnClickListener(this);
    }

    @Override public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_show:
                if (data != null && !data.isEmpty()) {
                    if (curPos > 9) {
                        curPos = 0;
                    }
 //                   loader.load(showImg, data.get(curPos).getUrl());
                    mSisterLoader.bindBitmap(data.get(curPos).getUrl(),showImg,400,400);
                    tv_name.setText("第 "+page+"页"+ " | "+"第 "+curPos+"张");
                    curPos++;
                }
                break;
            case R.id.btn_refresh:
                page++;
                new  SisterTask(page).execute();
                curPos = 0;
                Log.v("page:", ""+page);
                tv_name.setText("第 "+page+"页"+ " | "+"第 "+curPos+"张");
                break;
        }
    }

    private class SisterTask extends AsyncTask<Void,Void,ArrayList<Sister>> {
       private  int page;
        public SisterTask(int page) {
            this.page = page;
        }

        @Override
        protected ArrayList<Sister> doInBackground(Void... voids) {
            return sisterApi.fetchSister(10,page);
        }

        @Override
        protected void onPostExecute(ArrayList<Sister> sisters){
            super.onPostExecute(sisters);
            data.clear();
            data.addAll(sisters);
            page++;
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
