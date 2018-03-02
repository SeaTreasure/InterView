package com.lianluo.interview;

import android.app.Application;
import android.content.Context;

import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

/**
 * Created by Administrator on 2018/2/28.
 */

public class InterViewApplication extends Application {

    private RefWatcher refWatcher;

    public static RefWatcher getRefWatcher(Context context){
        InterViewApplication application= (InterViewApplication)
                context.getApplicationContext();
        return application.refWatcher;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        refWatcher=LeakCanary.install(this);
    }
}
