package com.lianluo.interview.service.download;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;

import com.lianluo.interview.R;
import com.lianluo.interview.activity.MainActivity;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DownLoadActivity extends Activity implements View.OnClickListener{

    @Bind(R.id.btn_start)
    Button btnStart;
    @Bind(R.id.btn_pause)
    Button btnPause;
    @Bind(R.id.btn_cancel)
    Button btnCancel;
    private DownLoadService.DownloadBinder downloadBinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_down_load);
        ButterKnife.bind(this);
        btnStart.setOnClickListener(this);
        btnPause.setOnClickListener(this);
        btnCancel.setOnClickListener(this);

        Intent intent = new Intent(this,DownLoadService.class);
        startService(intent);//开始服务
        bindService(intent,connection,BIND_AUTO_CREATE);//绑定服务

        //查看权限，如果没有则申请权限
        if(ContextCompat.checkSelfPermission(DownLoadActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(DownLoadActivity.this,new String[]{Manifest.permission .WRITE_EXTERNAL_STORAGE},1);
        }
    }


    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            downloadBinder = (DownLoadService.DownloadBinder) iBinder;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    public void onClick(View view) {
        if(downloadBinder ==null){
            return;
        }
        switch (view.getId()){
            case R.id.btn_start:
                String url = "https://raw.githubusercontent.com/guolindev/eclipse/master/" + "eclipse-inst-win64.exe";
                downloadBinder.startDownload(url);
                break;
            case R.id.btn_pause:
                downloadBinder.pauseDowload();
                break;
            case R.id.btn_cancel:
                downloadBinder.cancelDownload();
                break;
            default:
                break;
        }


    }

    @Override
    protected  void onDestroy(){
        super.onDestroy();
        unbindService(connection);
    }
}
