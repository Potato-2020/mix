package com.potato.mix

import com.potato.mix.extentions.MixExtension
import com.potato.mix.transform.MixTransform
import org.gradle.api.Plugin
import org.gradle.api.Project
import com.android.build.gradle.AppExtension
import org.gradle.api.artifacts.Configuration

class MixPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.repositories {
            google()
            jcenter()
            maven { url 'https://jitpack.io' }
        }
        project.dependencies {
            implementation 'com.github.Potato-2020:mixTools:v0.1'
        }
        //为工程添加mixTools依赖方式2
//        Configuration configuration = project.configurations.create('mixTools')
//        def notation = [group: 'com.github.Potato-2020', name: 'mixTools', version: 'v0.1']
//        project.dependencies.add(configuration.name, notation)
        //仅支持app工程
        if (project.plugins.hasPlugin("com.android.application")) {
            //创建混淆扩展块
            project.extensions.create("mix", MixExtension)
            //对AppExtension注册一个ASM任务
            project.extensions.getByType(AppExtension).registerTransform(new MixTransform(project))
        }
    }
}