package com.potato.asmmix;

import com.potato.mix.MixTemplate;

/**
 * create by Potato
 * create time 2020/8/6
 * Description：插桩模板类
 * */
@MixTemplate
public class Template {

    public static void mixOne() {
        System.out.println("插桩模板方法一！！！");
    }

    public static void mixTow() {
        System.out.println("我是插桩方法模板二");
    }
//    public void test(){}
}
