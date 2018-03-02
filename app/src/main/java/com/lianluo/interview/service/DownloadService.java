package com.lianluo.interview.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import static android.content.ContentValues.TAG;

/**
 * Created by Administrator on 2018/2/26.
 */

public class DownloadService extends Service {

    private String urlStr;
    private String fileRootPath = "";
    private String fileDownLoadPath = "";
    private File downloadDir;
    private File downloadFile;
    private File downloadFileTmp;
    private int fileSize;
    private NotificationManager nManager;
    private Notification.Builder mBuilder;
    protected static final int notifiID = 0x000;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        urlStr = intent.getStringExtra("signUrl");
        DownLoadFile(urlStr);
        return super.onStartCommand(intent, flags, startId);
    }

    private void DownLoadFile(String downloadUrl) {
        /*文件名*/
        final String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/") + 1);
        /*缓存文件*/
        String tempFile = "download.tmp";
        /*下载目录*/
        downloadDir = new File(fileRootPath + fileDownLoadPath);
        downloadFile = new File(fileRootPath + fileDownLoadPath + fileName);
        downloadFileTmp = new File(fileRootPath + fileDownLoadPath + tempFile);

        if (!downloadDir.exists()) {
            downloadDir.mkdirs();
        }
        /*文件存在，安装app*/
        if (downloadFile.exists()) {
            installApp(DownloadService.this, fileRootPath + fileDownLoadPath + fileName);
        } else {
            /*通知栏显示进度*/
            nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mBuilder = new Notification.Builder(this);
            mBuilder.setContentText("正在下载文件");
            mBuilder.setContentTitle("文件下载");
            mBuilder.setProgress(100, 0, false);
            mBuilder.setSmallIcon(android.R.drawable.stat_sys_download);

            /*下载文件*/
            new AsyncTask<String, Integer, String>() {
                @Override
                protected void onPreExecute() {
                    mBuilder.setTicker("下载电子签名").setProgress(100, 0, false);
                    nManager.notify(notifiID, mBuilder.build());
                    super.onPreExecute();
                }

                @Override
                protected void onProgressUpdate(Integer... values) {
                    Log.e(TAG, "---下载缓存" + values[0] + "---");
                    int pp = ((values[0] + 1) * 100 / fileSize);
                    mBuilder.setProgress(100, pp, false).setContentText("已下载" + pp + "%");
                    nManager.notify(notifiID, mBuilder.build());
                    super.onProgressUpdate(values);
                }

                @Override
                protected String doInBackground(String... params) {
                    try {
                        String fileName = params[0].substring(params[0].lastIndexOf("/") + 1);
                        URL url = new URL(params[0]);
                        URLConnection connection = url.openConnection();
                        connection.setDoInput(true);
                        connection.connect();
                        InputStream input = connection.getInputStream();
                        fileSize = connection.getContentLength();
                        if (fileSize <= 0) {
                            throw new RuntimeException("无法获知文件大小 ");
                        }
                        if (input == null) {
                            throw new RuntimeException("stream is null");
                        }
                        /*判断下载目录是否存在*/

                        if (!downloadDir.exists()) {
                            downloadDir.mkdirs();
                        }
                        FileOutputStream fileOpsTmp = new FileOutputStream(downloadFileTmp);
                        byte[] buf = new byte[1024];
                        int fileCache = 0;
                        do {
                            int numRead = input.read(buf);
                            if (numRead == -1) {
                                break;
                            }
                            fileOpsTmp.write(buf, 0, numRead);
                            fileCache += numRead;
                            this.publishProgress(fileCache);
                        } while (true);

                        try {
                            input.close();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return "下载成功";
                }

                @Override
                protected void onPostExecute(String s) {
                    /*下载成功后*/
                    if(downloadFileTmp.exists()){
                        downloadFileTmp.renameTo(downloadFile);
                    }
                    Toast.makeText(DownloadService.this, s, Toast.LENGTH_SHORT).show();
                    /*取消通知*/
                    mBuilder.setProgress(100,0,false);
                    nManager.cancel(notifiID);
                    //安装apk
                    installApp(DownloadService.this, fileRootPath + fileDownLoadPath + fileName);
                    /*关闭服务*/
                    DownloadService.this.stopSelf();
                    super.onPostExecute(s);
                }
            }.execute(downloadUrl);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //安装APP
    private void installApp(Context context,String filePath){
        File _file=new File(filePath);
        Intent intent=new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(_file),"application/vnd.android.package-archive");
        context.startActivity(intent);
    }
}
