package com.lianluo.interview.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.lianluo.interview.R;
import com.lianluo.interview.service.DownloadService;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bundle bundle = new Bundle();
        bundle.putString("signatureurl", "下载地址");/*电子签名下载地址*/
        Intent it = new Intent().setClass(this, DownloadService.class).putExtras(bundle);
        startService(it);
    }
}
