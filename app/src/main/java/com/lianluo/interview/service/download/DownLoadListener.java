package com.lianluo.interview.service.download;

/**
 * Created by Administrator on 2018/3/2.
 */

public interface DownLoadListener {
    void onProgress(int progress);
    void onLoadSuccess();
    void onLoadFailed();
    void onLoadPause();
    void onLoadResume();
    void onLoadStart();
    void onLoadCanceled();
}
