package com.potato.asmmix

import android.content.ContentResolver
import android.content.Context

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
    }

    //如果插桩会出现异常，说明ASM读取、写入字节码的时候，使用了标识：忽略堆栈中的帧（PS：可是如果不忽略，那TM的大批量方法进行插桩，又会影响性能！！！何解？？？）
    fun try2(context: Context) {
        try {
            var cr = context.contentResolver
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}