package com.potato.asmmix

import com.potato.asmmix.UriTest
import android.content.ContentResolver
import android.content.Context
import java.lang.Exception

/**
 * create by Potato
 * create time 2020/7/31
 * Description：try catch测试
 */
class TryCatchTest {

    fun try1(context: Context) {
        var cr: ContentResolver? = null
        try {
            cr = context.contentResolver
        } catch (e: Exception) {
            e.printStackTrace()
        }
    } //插桩会出现异常
    //    public void try2(Context context) {
    //        try {
    //            ContentResolver cr = context.getContentResolver();
    //        } catch (Exception e) {
    //            e.printStackTrace();
    //        }
    //    }
}