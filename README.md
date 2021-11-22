# mix
ASM马甲包插入junk code
# 打包效果
![a](https://github.com/Potato-2020/mix/blob/master/process.gif)
# 使用说明
**根目录下的build.gradle**

```groovy
buildscript {
    repositories {
        ......
        maven {
            url "https://dl.bintray.com/potato-2020/mixPlugin"
        }
        ......
        
    }
    dependencies {
        ......
        classpath 'com.potato.mix:mixPlugin:1.0.2'
        ......
    }
    allprojects {
    repositories {
        ......
        maven { url "https://jitpack.io" }
   		......
    }
}
}
```

**app下的build.gradle**

```groovy
apply plugin: 'mixplugin'
mix {
    isMix true//true:开启; fasle：关闭
    openLog true//true:开启ASM打印日志
    pathPre 'com.epoch.easyCoin'//凡是以这个开头的类，都会被插入一些模板代码
//    methodName 'onCreate'//针对某个方法混淆插桩
//    exclude 'com.epoch.easyCoin.mvvmEasy.debugEasy', "com.epoch.easyCoin.baseEasy"//这里的不插桩代码
}
depencies{
    ......
    implementation 'com.github.Potato-2020:mixTools:1.0.2'
}
```

**注入模板类代码**

```java
@MixTemplate
public class Template {

    public static void mixOne() {
        System.out.println("插桩模板方法一！！！");
    }

    public static void mixTow() {
        System.out.println("我是插桩方法模板二");
    }
}
```

**排除某个类**

```java
@MixExclude

public class Test {

}
```

**排除某个方法**

```java
@MixExcludeMethod
public void test() {

}
```

