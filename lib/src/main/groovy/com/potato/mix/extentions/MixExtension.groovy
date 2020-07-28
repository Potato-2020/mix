package com.potato.mix.extentions

/**
 * create by Potato
 * create time 2020/7/22
 * Description：混淆扩展块
 */
class MixExtension {

    def openLog//是否开启日志打印
    def pathPre//包名前缀
    def methodName//方法名
    def useAnnotation//是否使用注解的代码

    def openLog(boolean openLog) {
        this.openLog = openLog
    }

    def useAnnotation(boolean useAnnotation) {
        this.useAnnotation = useAnnotation
    }

    def pathPre(String pathPre) {
        this.pathPre = pathPre
    }

    def methodName(String methodName) {
        this.methodName = methodName
    }
}