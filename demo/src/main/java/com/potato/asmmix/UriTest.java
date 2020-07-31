package com.potato.asmmix;

import android.net.Uri;
import android.text.TextUtils;

/**
 * create by Potato
 * create time 2020/7/30
 * Description：
 */
public class UriTest {

    public void uriTest(String path) {
        Object b = null;
        if (!TextUtils.isEmpty(path)) {
            b = Uri.parse(path);
        } else {
            System.out.println();
        }
    }

    public void test2(String path) throws Exception {

        Object b = !TextUtils.isEmpty(path) ? Uri.parse(path) : null;
    }

}
