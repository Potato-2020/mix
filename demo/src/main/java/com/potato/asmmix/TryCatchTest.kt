package com.potato.asmmix;

import android.content.ContentResolver;
import android.content.Context;

/**
 * create by Potato
 * create time 2020/7/31
 * Description：try catch测试
 */
public class TryCatchTest {
    public void try1(Context context) {
        ContentResolver cr = null;
        try {
            cr = context.getContentResolver();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //插桩会出现异常
//    public void try2(Context context) {
//        try {
//            ContentResolver cr = context.getContentResolver();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
