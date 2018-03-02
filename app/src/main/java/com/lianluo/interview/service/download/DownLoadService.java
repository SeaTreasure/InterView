package com.lianluo.interview.service.download;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.lianluo.interview.R;
import com.lianluo.interview.activity.MainActivity;

import java.io.File;

/**
 * Created by Administrator on 2018/3/2.
 */

public class DownLoadService extends Service {

    private DownLoadTask downloadTask;

    private String downloadUrl;

    @Override
    public int onStartCommand(Intent intent,int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    private DownLoadListener listener = new DownLoadListener() {
        @Override
        public void onProgress(int progress) {
            getNotificationManager().notify(1, getNotification("Downloading...", progress));
        }

        @Override
        public void onLoadSuccess() {
            downloadTask = null;
            //下载成功将前台服务通知关闭，并创建一个下载成功的通知
            stopForeground(true);
            getNotificationManager().notify(1, getNotification("Download Success", -1));
            Toast.makeText(DownLoadService.this, "下载成功.在binder里面回调方法里面", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onLoadFailed() {
            downloadTask = null;
            //下载成功将前台服务通知关闭，并创建一个下载失败的通知
            stopForeground(true);
            getNotificationManager().notify(1, getNotification("Download Failed", -1));
            Toast.makeText(DownLoadService.this, "Download Failed", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onLoadPause() {
            downloadTask = null;
            Toast.makeText(DownLoadService.this, "Paused", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onLoadResume() {

        }

        @Override
        public void onLoadStart() {

        }

        @Override
        public void onLoadCanceled() {
            downloadTask = null;
            stopForeground(true);
            Toast.makeText(DownLoadService.this, "这里只取消不删除", Toast.LENGTH_SHORT).show();
        }


    };

    private DownloadBinder mBinder = new DownloadBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    //为了让服务和活动之间联系通信，创建Binder
    //提供三个，开始，暂停，取消的方法
    class DownloadBinder extends Binder {
        public void startDownload(String url) {
            if(downloadTask == null){
                downloadUrl = url;
                downloadTask = new DownLoadTask(listener);
                downloadTask.execute(downloadUrl);
                startForeground(1,getNotification("Downloading...",0));
                Toast.makeText(DownLoadService.this, "Downloading...开始下载binder里面", Toast.LENGTH_SHORT).show();
            }
        }

        public void pauseDowload(){
            if(downloadTask != null){
                downloadTask.pauseDownload();
            }
        }

        public void cancelDownload(){
            if(downloadTask != null){
                downloadTask.cancelDownload();
            }else {
                // 取消下载时，需要将之前的文件都删除。
                if(downloadUrl!= null){
                    String fileName = downloadUrl.substring(
                            downloadUrl.lastIndexOf("/"));
                    String directory = Environment.
                            getExternalStoragePublicDirectory
                                    (Environment.DIRECTORY_DOWNLOADS).getPath();
                    File file = new File(directory + fileName);
                    if(file.exists()){
                        file.delete();
                    }
                    getNotificationManager().cancel(1);
                    stopForeground(true);
                    Toast.makeText(DownLoadService.this, "这里取消下载也删除了", Toast.LENGTH_SHORT).show();
                }
            }
        }

    }

    private Notification getNotification(String title, int progress) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        builder.setContentIntent(pi);
        builder.setContentTitle(title);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);//设置通知重要程度，min,low,默认,high，max
        if (progress > 0) {
            builder.setContentText(progress + "%");
            builder.setProgress(100, progress, false);//三个参数，最大进度，当前进度，是否使用模糊进度条
        }
        return builder.build();
    }

    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }


}
