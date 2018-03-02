package com.lianluo.interview.service.download;

import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Administrator on 2018/3/2.
 */

public class DownLoadTask extends AsyncTask<String, Integer, Integer> {

    //定义四个下载状态常量
    public static final int TYPE_SUCCESS = 0;//下载成功
    public static final int TYPE_FAILED = 1;//下载失败
    public static final int TYPE_PAUSED = 2;//下载暂停
    public static final int TYPE_CANCELED = 3;//下载取消

    private int lastProgress;

    private DownLoadListener downloadListener;

    private boolean isCanceled = false;
    private boolean isPaused = false;

    private InputStream is;
    private RandomAccessFile accessFile;
    private File file;

    public DownLoadTask(DownLoadListener downloadListener) {
        this.downloadListener = downloadListener;
    }

    public void pauseDownload(){
        isPaused = true;
    }

    public void cancelDownload(){
        isCanceled = true;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(Integer[] values) {
        int progress = (int) values[0];
        if (progress > lastProgress) {
            //回调方法中的onProgress
            downloadListener.onProgress(progress);
            lastProgress = progress;
        }
        super.onProgressUpdate(values);
    }

    @Override
    protected Integer doInBackground(String[] params) {
        try {
            long downLoadedLenth = 0;
            String downLoadUrl = (String) params[0];
            String fileName = downLoadUrl.substring(downLoadUrl.lastIndexOf("/"));
            String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
            file = new File(directory + fileName);
            if (file.exists()) {
                downLoadedLenth = file.length();
            }
            long contentLength = 0;

            contentLength = getContentLength(downLoadUrl);

            if (contentLength == 0) {
                return TYPE_FAILED;
            } else if (contentLength == downLoadedLenth) {
                //已下载字节和总文件字节长度相等，则下载成功
                return TYPE_SUCCESS;
            }

            OkHttpClient okHttpClient = new OkHttpClient();
            Request request = new Request.Builder().addHeader("RANGE", "bytes = " + downLoadedLenth + "-").url(downLoadUrl).build();

            Response response=okHttpClient.newCall(request).execute();
            if(request!=null){
                is=response.body().byteStream();
                accessFile=new RandomAccessFile(file,"rw");
                accessFile.seek(downLoadedLenth);
                byte[] b=new byte[1024];
                int total=0;
                int len;
                while ((len=is.read(b))!=-1){
                    if (isCanceled) {
                        return TYPE_CANCELED;
                    } else if (isPaused) {
                        return TYPE_PAUSED;
                    } else {
                        total+=len;
                        accessFile.write(b,0,len);
                        //计算已下载的百分比
                        int progress = (int) ((total + downLoadedLenth) * 100 / contentLength);
                        publishProgress(progress);
                    }
                }
                response.body().close();
                return TYPE_SUCCESS;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (accessFile != null) {
                    accessFile.close();
                }
                if (isCanceled && file != null) {
                    file.delete();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return TYPE_FAILED;
    }

    @Override
    protected void onPostExecute(Integer status) {
        switch (status) {
            case TYPE_SUCCESS:
                downloadListener.onLoadSuccess();
                break;
            case TYPE_FAILED:
                downloadListener.onLoadFailed();
                break;
            case TYPE_PAUSED:
                downloadListener.onLoadPause();
                break;
            case TYPE_CANCELED:
                downloadListener.onLoadCanceled();
                break;
            default:
                break;
        }
        super.onPostExecute(status);
    }

    private long getContentLength(String downloadUrl) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(downloadUrl).build();
        Response response = client.newCall(request).execute();
        if (response != null && response.isSuccessful()) {
            long contentLength = response.body().contentLength();
            response.close();
            return contentLength;
        }
        return 0;
    }
}
