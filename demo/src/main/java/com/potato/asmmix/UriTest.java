package com.potato.asmmix;

import android.net.Uri;
import android.text.TextUtils;

import com.potato.asmmix.dialog.GuideOneDialog;

/**
 * create by Potato
 * create time 2020/7/30
 * Description： Object测试
 * */
public class UriTest {

    public void uriTest(String path) {
        Object b = !TextUtils.isEmpty(path) ? Uri.parse(path) : null;
    }

    //插桩会出现异常
//    public void test2(String path) throws Exception {
//
//        Object b = !TextUtils.isEmpty(path) ? Uri.parse(path) : "随便";
//    }

}
