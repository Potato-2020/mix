package com.potato.mix.transform

import com.android.build.api.transform.*
import com.android.utils.FileUtils
import com.google.common.collect.ImmutableSet
import com.potato.mix.extentions.MixExtension

import org.apache.commons.codec.digest.DigestUtils
import org.objectweb.asm.*
import org.objectweb.asm.commons.AdviceAdapter
import org.objectweb.asm.commons.Method
import org.gradle.api.Project

class MixTransform extends Transform {

    private Project project
    private static String pathPre//包名前缀
    private static boolean openLog//是否开启日志打印
    private static String mixMethodName//方法名


    MixTransform(Project project) {
        this.project = project
        MixExtension mixExtension = project.mix
        project.afterEvaluate {
            pathPre = mixExtension.pathPre
            mixMethodName = mixExtension.methodName
            openLog = mixExtension.openLog
            if (openLog) {
                log("获取扩展块成功： pathPre: ${pathPre}; methodName: ${mixMethodName}; openLog: $openLog");
            }
        }
    }

    /**
     * 代表该Transform的task的名字
     *
     * @return name
     */
    @Override
    public String getName() {
        return "MixTransform"
    }

    /**
     * 指明Transform的输入类型(这里返回CLASSES，只处理字节码)
     *
     * @return 输入类型
     */
    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return ImmutableSet.of(QualifiedContent.DefaultContentType.CLASSES)
    }

    /**
     * 指明Transform的作用域（整个项目）
     *
     * @return 作用域
     */
    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        return ImmutableSet.of(QualifiedContent.Scope.PROJECT)
    }

    /**
     * 是否支持增量编译
     *
     * @return false：不支持
     */
    @Override
    public boolean isIncremental() {
        return false
    }

    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        //获取输入文件（这里只处理了ClASSES类型）
        transformInvocation.getInputs().each { TransformInput input ->
            //处理所有jar包
            input.jarInputs.each { JarInput jarInput ->
                String destName = jarInput.name
                if (destName.endsWith(".jar")) {
                    destName = destName.substring(0, destName.length() - 4)
                }
                File src = jarInput.file
                def hex = DigestUtils.md5Hex(jarInput.file.absolutePath)
                File dest = transformInvocation.getOutputProvider().getContentLocation(destName + "_" + hex, jarInput.contentTypes, jarInput.scopes, Format.JAR)
//                log("jar包的path：${src.absolutePath}")
//                if (shouldProcessPreDexJar(src.absolutePath)) {
//                    todothing
//                    def srcJar = new JarFile(src)
//                    Enumeration enumeration = srcJar.entries()
//                    while (enumeration.hasMoreElements()) {
//                        JarEntry jarEntry = enumeration.nextElement()
                //取出每一个class类，注意这里的包名是"/"分割 ，不是"."
//                        String entryName = jarEntry.name
                //todothing
//                        if (entryName.startsWith("com.demo.lib.asmdemo")) {
//                            InputStream inputStream = srcJar.getInputStream(jarEntry)
//                            //从jar中取出对应的输入流
//                            byte [] bytes = scanClass(inputStream) //jar包需要copy一个临时包最后再rename
//                            inputStream.close()
//                        }
//                    }
//                }

                FileUtils.copyFile(src, dest)
            }
            //处理工程目录，以及工程字节码（项目源文件）
            input.directoryInputs.each { DirectoryInput directoryInput ->
                //分别遍历app/build/intermediates/classes/debug下的每个目录
                String root = directoryInput.file.absolutePath
                if (!root.endsWith(File.separator)) {
                    root += File.separator
                }
                directoryInput.file.eachFileRecurse { File file ->
                    //遍历app/build/intermediates/classes/debug/*/ 下的每一个文件
                    def path = file.absolutePath.replace(root, '') //取出path看下包名是否是符合的包名信息
                    if (!(File.separator == '/')) {
                        path = path.replaceAll("\\\\", ".")
                    }
//                    System.out.print("包名信息$path")
                    if (file.isFile() && shouldProcessClass(path)) {
                        FileInputStream fis = new FileInputStream(file)
                        byte[] bytes = scanClass(fis, file.parentFile.absolutePath + File.separator + file.name)
//                        log("MixPlugin>>>输出地址：" + file.parentFile.absolutePath + File.separator + file.name)
                        FileOutputStream fos = new FileOutputStream(file.parentFile.absolutePath + File.separator + file.name)
                        fos.write(bytes)
                        fos.close()
                        fis.close()
                    }
                }
                File dest = transformInvocation.getOutputProvider().getContentLocation(
                        directoryInput.name,
                        directoryInput.contentTypes,
                        directoryInput.scopes,
                        Format.DIRECTORY)
                FileUtils.copyDirectory(directoryInput.file, dest)
            }
        }
    }

    /**
     * 判断是否需要扫描classFilePath
     *
     * @param classFilePath 扫描的class文件的路径
     */
    boolean shouldProcessClass(String classFilePath) {
        //这里写死了包名
        return classFilePath != null && classFilePath.startsWith(pathPre)
    }

    /**
     * 判断是否需要扫描jar文件
     *
     * @param jarFilepath jar文件的路径
     */
//    private static boolean shouldProcessPreDexJar(String jarFilepath) {
//        return (null != jarFilepath && "" != jarFilepath)
//                && !jarFilepath.contains("com.androidx")
//                && !jarFilepath.contains("com.android.support")
//                && !jarFilepath.contains("/android/m2repository")
//    }

    private byte[] scanClass(InputStream inputStream, String className) throws IOException {
        //事件处理流程：读取事件-->适配器-->编写器
        //事件产生
        ClassReader cr = new ClassReader(inputStream)
        //事件处理
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES)
        //代理处理，并转发给cw
        ScanClassVisitor cv = new ScanClassVisitor(Opcodes.ASM5, cw, className)  //ClassWriter 的代理类
        cr.accept(cv, ClassReader.EXPAND_FRAMES)
        return cw.toByteArray()
    }

    /**
     * 操作Class，找到需要插桩的XX方法
     */
    static class ScanClassVisitor extends ClassVisitor {

        String className

        ScanClassVisitor(int api, ClassVisitor cv, String className) {
            super(api, cv)
            this.className = className
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces)
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions)
            if (null != mixMethodName && "" != mixMethodName) {
                if (mixMethodName == name) {
                    //匹配对应的方法名字，进行插桩
                    mv = new MixAdviceAdapter(Opcodes.ASM5, mv, access, name, desc)
                }
            } else {
                //没有配置方法名称，无差别插桩
                mv = new MixAdviceAdapter(Opcodes.ASM5, mv, access, name, desc)
            }
            log('=========================================================START==================================================================')
            log("${className}>>>$name")
            return mv
        }
    }


    /**
     * 操作XX方法的字节码，插入代码(手动插入，自己指定位置)
     */
    static class ScanMethodVisitor extends MethodVisitor {

        ScanMethodVisitor(int api, MethodVisitor mv) {
            super(api, mv)
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            mv.visitMethodInsn(opcode, owner, name, desc, itf)
        }

    }

    /**
     * 插入代码（方法前后都插入）
     */
    static class MixAdviceAdapter extends AdviceAdapter {

        String methodName

        protected MixAdviceAdapter(int api, MethodVisitor mv, int access, String name, String desc) {
            super(api, mv, access, name, desc)
            methodName = name
        }

        /**
         * 访问注解
         * @param desc
         * @param visible
         * @return
         */
        @Override
        AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            return super.visitAnnotation(desc, visible)
        }

        @Override
        protected void onMethodEnter() {
            if (!methodName.contains("<init>")) {
                log("方法前插入")
                log('=========================================================OVER==========================================================\n\n')
                getStatic(Type.getType("Ljava/lang/System;"), "out", Type.getType("Ljava/io/PrintStream;"))
                visitLdcInsn("Let's go")
                invokeVirtual(Type.getType("Ljava/io/PrintStream;"), new Method("println", "(Ljava/lang/String;)V"))
//            mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
//            mv.visitLdcInsn("========start=========")
            }
        }

        @Override
        protected void onMethodExit(int opcode) {
            if (methodName.contains("<init>")) {
                log('方法后插入')
                log('=========================================================OVER==================================================================\n\n')
                getStatic(Type.getType("Ljava/lang/System;"), "out", Type.getType("Ljava/io/PrintStream;"))
                visitLdcInsn("Let's go")
                invokeVirtual(Type.getType("Ljava/io/PrintStream;"), new Method("println", "(Ljava/lang/String;)V"))
//                mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;")
//                mv.visitLdcInsn("========start=========")
            }
        }
    }

    /**
     * 打印日志
     */
    static def log (String msg) {
        if (openLog) {
            System.out.println(msg)
        }
    }

}