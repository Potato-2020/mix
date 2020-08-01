package com.potato.asmmix

import java.util.ArrayList

/**
 * create by Potato
 * create time 2020/7/31
 * Description：
 */
class KotlinTest {
    private var listBean: ListBean? = null

    fun test1() {
        val result1 = listBean?.childBean
        var result2 = result1?.child
    }

//    fun test2() {
//        var result = listBean?.childBean?.child
//    }

    internal class ListBean {
        var childBean: ChildBean? = null
    }

    internal class ChildBean {
        var child = 0
    }
}