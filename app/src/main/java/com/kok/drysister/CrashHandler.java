package com.kok.drysister;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;

class CrashHandler implements Thread.UncaughtExceptionHandler{
    private static final String TAG = "CrashHandler";
    private static CrashHandler instance;
    private Thread.UncaughtExceptionHandler mDefaultHandler;//系统默认UncaughtExceptionHandler
    private Context mContext;
    private Map<String, String> infos = new HashMap<>();//用于存储设备信息和异常信息
    private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

    private CrashHandler() {
    }
    public static CrashHandler getInstance() {
        if (instance == null) {
            instance = new CrashHandler();
        }
        return instance;
    }

    public void init(Context context) {
        mContext = context;
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }
    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        if (!handleException(e) && mDefaultHandler != null) {
            mDefaultHandler.uncaughtException(t, e);
        } else {
            AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(mContext, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("crash", true);
            PendingIntent restartIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_ONE_SHOT);
            alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, restartIntent);//一秒钟后重启应用

            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
            System.gc();
        }
    }

    /**
     * 自定义错误处理，错误信息采集，日志文件保存，如果处理了返回true，否则返回false
     * @param e
     * @return
     */
    private boolean handleException(Throwable e) {
        if (e == null) {
            return false;
        }
        try {
            new Thread() {
                @Override
                public void run() {
                    Looper.prepare();
                    Toast.makeText(mContext, "程序出现异常，即将重启.", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
            }.start();
            getDeviceInfo(mContext);
            saveCrashInfoToFile(e);
            SystemClock.sleep(1000);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return true;
    }

    /**
     * 把错误信息写入文件，返回文件名称
     * @param throwable
     */
    private String saveCrashInfoToFile(Throwable throwable)throws Exception {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            //拼接时间信息
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String date = simpleDateFormat.format(new java.util.Date());
            stringBuilder.append("\r\n").append(date).append("\n");
            //拼接版本信息和设备信息
            for (Map.Entry<String, String> entry : infos.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                stringBuilder.append(key).append("=").append(value).append("\n");
            }
            //获取崩溃日志
            Writer writer = new StringWriter();
            PrintWriter printWriter = new PrintWriter(writer);
            throwable.printStackTrace(printWriter);
            printWriter.flush();
            printWriter.close();
            String result = writer.toString();
            //拼接崩溃日志
            stringBuilder.append(result);
            //写入到文件中
            return writeFile(stringBuilder.toString());
        } catch (Exception e) {
            //异常处理
            Log.e(TAG, "an error occurred while writing file... ", e);
            stringBuilder.append("an error occurred while writing file...\r\n");
            writeFile(stringBuilder.toString());
        }
        return null;
    }


    /**
     * 获取Crash文件夹到存储路径
     * @return
     */
    private static String getGlobalPath() {
        // TODO: 2020/6/20 现有方法获取路径错误,暂不使用,待 获取正确的应用路径 的实现
        return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
                + "Crash" + File.separator;
    }

    private String writeFile(String sb) throws Exception{
        String time = formatter.format(new Date());
        //文件名
        String fileName = "crash-" + time + ".log";
        //判断存储卡是否可用
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            String path = "/storage/emulated/0/Android/data/com.kok.drysister/Crash/";
            File dir = new File(path);
            dir.mkdirs();
            //判断crash目录是否存在
            if (!dir.exists())  dir.mkdirs();

            //流写入
            FileOutputStream fos = new FileOutputStream(path + fileName, true);
            fos.write(sb.getBytes());
            fos.flush();
            fos.close();
        }
        return fileName;
    }

    /**
     * 采集应用版本与设备信息
     * @param context
     */
    private void getDeviceInfo(Context context) {
        //获取APP版本
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (packageInfo != null) {
                infos.put("VersionName", packageInfo.versionName);
                infos.put("VersionCode", packageInfo.versionCode + "");
            }
        } catch (PackageManager.NameNotFoundException e) {
            LogUtils.e(TAG,"an error occurred when collect package info");
        }
        //获取系统设备相关信息
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                infos.put(field.getName(), field.get(null).toString());
            } catch (Exception e) {
                LogUtils.e(TAG,"an error occurred when collect package info");
            }
        }
    }

}
