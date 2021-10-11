# mix
ASM混淆
# 使用说明
**根目录下的build.gradle**

```groovy
buildscript {
    repositories {
        ......
        ......
        
    }
    dependencies {
        ......
        classpath 'com.potato.mix:mixPlugin:1.0.0'
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
    isMix true//开启混淆插桩插件
    openLog true//开启日志打印
    pathPre 'com.potato.asmmix'//混淆插桩这个包下的所有文件
//    methodName 'onCreate'//混淆插桩某个方法
//    exclude 'com.potato.asmmix.dialog'//排除这些包下的文件
}
depencies{
    ......
    implementation 'com.github.Potato-2020:mixTools:1.0'
}
```

**注入模板类代码**

```java
/**
* 注意，此模板类中所有的public static 修饰的声明的方法，都会执行插桩操作。
* 如果不想插入方法，使用private或者非static修饰。
*/
@MixTemplate
public class Template {

    public static void mixOne() {
        System.out.println("插桩模板方法一！！！");
    }

    public static void mixTow() {
        System.out.println("我是插桩方法模板二");
    }
//    private void mixTow() {
//        System.out.println("这个方法不会进行插桩");
//    }

}
```

**排除某个类**

```java
@MixExclude

public class Test {

}
```
