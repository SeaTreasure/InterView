package com.lianluo.interview.base;

import android.app.Fragment;

import com.lianluo.interview.InterViewApplication;
import com.squareup.leakcanary.RefWatcher;

/**
 * Created by Administrator on 2018/2/28.
 */

public class BaseFragment extends Fragment {
    @Override
    public void onDestroy() {
        super.onDestroy();
        RefWatcher refWatcher=InterViewApplication.getRefWatcher(getActivity());
        refWatcher.watch(this);
    }
}
