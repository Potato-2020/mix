package com.potato.asmmix;

import android.content.ContentResolver;
import android.content.Context;

/**
 * create by Potato
 * create time 2020/7/31
 * Description：
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

    public void try2(Context context) {
        try {
            ContentResolver cr = context.getContentResolver();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
