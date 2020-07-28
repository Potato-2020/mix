package com.potato.asmmix;


import com.potato.mix.MixClass;
import com.potato.mix.MixCode;

/**
 * create by Potato
 * create time 2020/7/25
 * Description：注解代码类，插桩的代码从这里获取
 */
@MixClass
public class MixCodeTest {
    @MixCode
    public void mixOne(String s) {
        System.out.println(s);
    }
}
