package com.potato.asmmix

import android.app.Activity
import com.potato.mix.MixExclude
import java.util.*
import kotlin.collections.ArrayList

/**
 * create by Potato
 * create time 2020/7/31
 * 加上@MixExclude后，插桩操作对EmptyTest无效
 * Description：判空操作测试
 */
@MixExclude
class EmptyTest {
    private var list: MutableList<Activity?> = Collections.synchronizedList(ArrayList())

    fun forTest() {
        if (list.isEmpty()) return
        for (item in list) {
            item?.finish()

        }
    }

//    fun forTest2() {
//        if (list.isNotEmpty()) {
//            for (item in list) {
//                item?.finish()
//            }
//        }
//    }
}