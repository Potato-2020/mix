package com.potato.mix

import com.potato.mix.extentions.MixExtension
import com.potato.mix.transform.MixTransform
import org.gradle.api.Plugin
import org.gradle.api.Project
import com.android.build.gradle.AppExtension

class MixPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        //仅支持app工程
        if (project.plugins.hasPlugin("com.android.application")) {
            //创建混淆扩展块
            project.extensions.create("mix", MixExtension)
            //对AppExtension注册一个ASM任务
            project.extensions.getByType(AppExtension).registerTransform(new MixTransform(project))
        }
    }
}