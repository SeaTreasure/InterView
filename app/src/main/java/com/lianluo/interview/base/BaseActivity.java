package com.lianluo.interview.base;

import android.app.Activity;

import com.lianluo.interview.InterViewApplication;

/**
 * Created by Administrator on 2018/2/28.
 */

public class BaseActivity extends Activity {

    @Override
    protected void onDestroy() {
        super.onDestroy();
        InterViewApplication.getRefWatcher(this).watch(this);
    }
}
