package com.example.jwbauer3.mapsourcing;

import android.content.Context;
import android.support.v4.content.ContextCompat;

/**
 * Created by Eric on 12/14/15.
 */
public class Application extends android.app.Application {
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        Application.context = this.getApplicationContext();
    }

    public static Context getContext() {
        return Application.context;
    }

    public static int getResColor(int resourceId) {
        return ContextCompat.getColor(Application.context, resourceId);
    }
}
