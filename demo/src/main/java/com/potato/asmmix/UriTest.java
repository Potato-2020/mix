package com.potato.asmmix;

import android.net.Uri;
import android.text.TextUtils;

/**
 * create by Potato
 * create time 2020/7/30
 * Description： Object测试
 * */
public class UriTest {

    public void uriTest(String path) {
        Object b = null;
        if (!TextUtils.isEmpty(path)) {
            b = Uri.parse(path);
        } else {
            System.out.println();
        }
    }

    //插桩会出现异常
//    public void test2(String path) throws Exception {
//
//        Object b = !TextUtils.isEmpty(path) ? Uri.parse(path) : "随便";
//    }

}
