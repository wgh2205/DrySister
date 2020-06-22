package com.kok.drysister;

import android.app.Application;
import android.content.Context;

public class DrySisterAPP extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        CrashHandler.getInstance().init(this);
    }

    public static Context getContext() {
        return (DrySisterAPP)context;
    }
}
