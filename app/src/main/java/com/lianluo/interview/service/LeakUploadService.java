package com.lianluo.interview.service;

import com.squareup.leakcanary.AnalysisResult;
import com.squareup.leakcanary.DisplayLeakService;
import com.squareup.leakcanary.HeapDump;

/**
 * Created by Administrator on 2018/2/28.
 */

public class LeakUploadService extends DisplayLeakService {

    @Override
    protected void afterDefaultHandling(HeapDump heapDump, AnalysisResult result, String leakInfo) {
        super.afterDefaultHandling(heapDump, result, leakInfo);
        if(!result.leakFound||result.excludedLeak){
            return;
        }
        //myServer.uploadLeakBlocking(heapDump.heapDumpFile, leakInfo);
    }
}
