package com.potato.mix.transform

import com.android.build.api.transform.*
import com.android.utils.FileUtils
import com.google.common.collect.ImmutableSet
import com.potato.mix.extentions.MixExtension
import org.apache.commons.codec.digest.DigestUtils
import org.gradle.api.Project
import org.objectweb.asm.*
import org.objectweb.asm.commons.AdviceAdapter
import org.objectweb.asm.commons.Method

import java.util.jar.JarEntry
import java.util.jar.JarFile

class MixTransform extends Transform {

    private Project project
    private static String pathPre//包名前缀
    private static boolean openLog//是否开启日志打印
    private static String mixMethodName//方法名
    private static ArrayList exclude//排除class文件
    private static boolean isMix//是否开启插件
    private static boolean excludeMethod = true//是否排除方法, true:已经排除了
    private static String type = ""//模板类的Type

    //存储插桩方法内容
    private static Map<String, List<Map<String, String>>> methodMapList = new HashMap<>()
    private static List<Map<String, String>> methodList = new ArrayList<>()
    //存储额外需要进行插桩的文件
    private static Map<String, DirectoryInput> inputCacheMap = new HashMap<>()

    MixTransform(Project project) {
        this.project = project
        MixExtension mixExtension = project.mix
        project.afterEvaluate {
            pathPre = mixExtension.pathPre
            mixMethodName = mixExtension.methodName
            openLog = mixExtension.openLog
            exclude = mixExtension.exclude
            isMix = mixExtension.isMix
            if (isMix && openLog) {
                log("开始混淆插入代码： pathPre: ${pathPre}; methodName: ${mixMethodName}; openLog: ${openLog}; exclude: $exclude")
                for (Map.Entry<String, List<Map<String, String>>> entry : methodMapList.entrySet()) {
                    if (null == entry || null == entry.value || entry.value.size() == 0) return
                    entry.value.clear()
                }
                type = ""
                methodMapList.clear()
                inputCacheMap.clear()
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
        if (isMix) {
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
//                log("jar文件>>>${src.absolutePath}")
                    if (shouldProcessPreDexJar(src.absolutePath)) {
                        def srcJar = new JarFile(src)
                        Enumeration enumeration = srcJar.entries()
                        while (enumeration.hasMoreElements()) {
                            JarEntry jarEntry = enumeration.nextElement()
                            //取出每一个class类，注意这里的包名是"/"分割 ，不是"."
                            String entryName = jarEntry.name
                            //todothing
//                        if (entryName.startsWith(pathPre)) {
//                            InputStream inputStream = srcJar.getInputStream(jarEntry)
//                            //从jar中取出对应的输入流
//                            byte [] bytes = scanClass(inputStream) //jar包需要copy一个临时包最后再rename
//                            inputStream.close()
//                        }
                        }
                    }
                    FileUtils.copyFile(src, dest)
                }
                //处理工程目录，以及工程字节码（项目源文件）
                input.directoryInputs.each { DirectoryInput directoryInput ->
                    //分别遍历app/build/intermediates/classes/debug下的每个目录
                    String root = directoryInput.file.absolutePath
                    if (!root.endsWith(File.separator)) {
                        root += File.separator
                    }
                    processASM(directoryInput, root)
                    processCopy(transformInvocation, directoryInput)
                }
            }
            //存在漏掉的文件，额外ASM处理一遍
            inputCacheMap.each { entry ->
                DirectoryInput directoryInput = entry.value
                String keyPath = entry.key
                String root = directoryInput.file.absolutePath
                if (!root.endsWith(File.separator)) {
                    root += File.separator
                }
                directoryInput.file.eachFileRecurse { File file ->
                    def path = file.absolutePath.replace(root, '') //取出path看下包名是否是符合的包名信息
                    if (!(File.separator == '/')) {
                        path = path.replaceAll("\\\\", "/")
                    }
                    if (keyPath == path) {
                        log("MixPlugin>>>处理漏掉的文件，再次插桩，path：$keyPath")
                        FileInputStream fis = new FileInputStream(file)
                        byte[] bytes = scanClass(fis, file.parentFile.absolutePath + File.separator + file.name)
                        FileOutputStream fos = new FileOutputStream(file.parentFile.absolutePath + File.separator + file.name)
                        fos.write(bytes)
                        fos.close()
                        fis.close()
                        processCopy(transformInvocation, directoryInput)
                    }
                }
            }
        }
    }

    /**
     * ASM核心处理
     */
    private static List processASM(DirectoryInput directoryInput, String root) {
        directoryInput.file.eachFileRecurse { File file ->
            //遍历app/build/intermediates/classes/debug/*/ 下的每一个文件
            def path = file.absolutePath.replace(root, '') //取出path看下包名是否是符合的包名信息
            if (!(File.separator == '/')) {
                path = path.replaceAll("\\\\", "/")
            }
            if (file.isFile() && checkExclude(path) && shouldProcessClass(path) && shouldProcessClassName(file.name)) {
                FileInputStream fis = new FileInputStream(file)
                byte[] bytes = scanClass(fis, file.parentFile.absolutePath + File.separator + file.name)
                FileOutputStream fos = new FileOutputStream(file.parentFile.absolutePath + File.separator + file.name)
                fos.write(bytes)
                fos.close()
                fis.close()
                if (type == "") {
                    inputCacheMap.put(path, directoryInput)
                    log("MixPlugin>>>漏掉了一个文件：path: $path")
                }
            }
        }
    }

    /**
     * ASM处理之后拷贝文件
     */
    private static void processCopy(TransformInvocation transformInvocation, DirectoryInput directoryInput) {
        File dest = transformInvocation.getOutputProvider().getContentLocation(
                directoryInput.name,
                directoryInput.contentTypes,
                directoryInput.scopes,
                Format.DIRECTORY)
        FileUtils.copyDirectory(directoryInput.file, dest)
    }

    /**
     * 如果配置了pathPre，就只处理对应的class文件
     *
     * @param classFilePath 扫描的class文件的路径
     */
    private static boolean shouldProcessClass(String classFilePath) {
        return classFilePath != null && classFilePath.startsWith(pathPre.replaceAll("\\.", "/"))
    }

    /**
     * 检查是排除的包名
     * @return true：检查通过
     */
    private static boolean checkExclude(String classFilePath) {
        boolean isChecked = true
        if (exclude.size() == 0) {
            isChecked = true
        } else {
            exclude.each { String dir ->
                if (classFilePath.startsWith(dir.replaceAll("\\.", "/"))) {
                    log("MixPlugin>>>捕捉到了Exclude，文件path：$classFilePath")
                    isChecked = false
                }
            }
        }
        return isChecked
    }

    /**
     * 判断是否需要扫描jar文件
     *
     * @param jarFilepath jar文件的路径
     */
    private static boolean shouldProcessPreDexJar(String jarFilepath) {
        boolean should = !jarFilepath.contains("com.androidx") && !jarFilepath.contains("com.android.support") && !jarFilepath.contains("/android/m2repository")
        return should
    }

    /**
     * 排除一些class文件
     * @param name 文件名
     * @return
     */
    static boolean shouldProcessClassName(String name) {
        return name.endsWith(".class") && !name.startsWith("R\$") && !name.contains("\$") &&
                "R.class" != name && "BuildConfig.class" != name && !name.endsWith("Binding.class")
    }

    private static byte[] scanClass(InputStream inputStream, String className) throws IOException {
        if (type != "") {
            log("==================================================================MixPlugin==================================================================")
            log("MixPlugin>>>插桩目标：$className")
        }
        //事件处理流程：读取事件-->适配器-->编写器
        //事件产生
        ClassReader cr = new ClassReader(inputStream)
        //事件处理
        //COMPUTE_MAXS:kotlin中的双问号使用，或者try catch代码块中的类声明，都不会报错（原因：不会忽略堆栈中的帧）
        //COMPUTE_FRAMES:与上面相反（原因：会忽略堆栈中的帧，需要重新计算）
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS)
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
        boolean isInterface
        boolean isAbstract
        boolean isMix = true//true：默认混淆代码插桩
        boolean isTemplateClass = false//true：插桩模板类

        ScanClassVisitor(int api, ClassVisitor cv, String className) {
            super(api, cv)
            this.className = className
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces)
            isInterface = (access & Opcodes.ACC_INTERFACE) != 0
            isAbstract = (access & Opcodes.ACC_ABSTRACT) != 0
        }

        /**
         * 如果类没有注解，不会调用此方法
         */
        @Override
        AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            if ("Lcom/potato/mix/MixExclude;" == descriptor) {
                isMix = false
            }
            if ("Lcom/potato/mix/MixTemplate;" == descriptor) {//模板类
                isTemplateClass = true
                findType()
            }
            return super.visitAnnotation(descriptor, visible)
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions)
            if (isTemplateClass) {//模板类不参与插桩
                if (excludeMethodTemp(name)) {
                    boolean canGetMethod = access == (Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC)
                    //public static的方法才符合存储条件
                    if (!canGetMethod) return mv
                    //存储结构如下：一个type对应着一个List，List里装着每个方法的name、desc、type
                    Map<String, String> methodMap = new HashMap<>()
                    methodMap.put("type", type)//"Lcom/potato/asmmix/MixTemplate;"
                    methodMap.put("name", name)
                    methodMap.put("desc", desc)
                    methodList.add(methodMap)
                    methodMapList.put(type, methodList)
                }
                return mv
            }
            if (!isInterface && !isAbstract && isMix && excludeMethod(name)) {
                if (null != mixMethodName && "" != mixMethodName) {
                    if (mixMethodName == name) {
                        //匹配对应的方法名字，进行插桩
                        mv = new MixAdviceAdapter(Opcodes.ASM5, mv, access, name, desc)
                    }
                } else {
                    //没有配置方法名称，无差别插桩
                    mv = new MixAdviceAdapter(Opcodes.ASM5, mv, access, name, desc)
                }
            }
            return mv
        }

        /**
         * 找到type
         * @return
         */
        private def findType() {
            if (null == pathPre) {
                log("MixPlugin>>>没有找到模板类，请确保模板类的包名是否是：${pathPre}")
                return
            }
            String[] strs = pathPre.replaceAll("\\.", "/").split("/")
            String splitType = ""
            if (strs.length > 0) {
                splitType = strs[0]
            }
            if (splitType == "" || null == className) return
            //以传入的className的第一个词作为分隔，取出并拼接成type-->Lcom/potato/asmmix/MixTemplate;
            String[] typeStrs = className.split(splitType)
            if (typeStrs.length == 0) {
                log("MixPlugin>>>没有找到模板类，请确保模板类的包名是否是：${pathPre}")
                return
            }
            String result = "L${splitType}${typeStrs[1]}"
            type = "${result.substring(0, result.length() - 6)};".replace("\\", "/")
            log("MixPlugin>>>找到了模板类type=$type")
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
            excludeMethod = true
        }

        /**
         * 访问注解
         * @param desc
         * @param visible
         * @return
         */
        @Override
        AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            if ("Lcom/potato/mix/MixExcludeMethod;" == desc) {
                excludeMethod = false
            }
            return super.visitAnnotation(desc, visible)
        }

        @Override
        protected void onMethodEnter() {
            if (type != "" && !methodName.contains("<init>")) {
                insertTemplate()
//                log("方法前插入")
//                for (i in 0..3) {
//                    getStatic(Type.getType("Ljava/lang/System;"), "out", Type.getType("Ljava/io/PrintStream;"))
//                    visitLdcInsn("Let's go")
//                    invokeVirtual(Type.getType("Ljava/io/PrintStream;"), new Method("println", "(Ljava/lang/String;)V"))
//                }
//                log('=========================================================OVER==========================================================\n\n')
            }
        }

        @Override
        protected void onMethodExit(int opcode) {
            if (type != "" && methodName.contains("<init>")) {
                insertTemplate()
//                log('方法后插入')
//                for (i in 0..3) {
//                    getStatic(Type.getType("Ljava/lang/System;"), "out", Type.getType("Ljava/io/PrintStream;"))
//                    visitLdcInsn("Sorry, I'm tired")
//                    invokeVirtual(Type.getType("Ljava/io/PrintStream;"), new Method("println", "(Ljava/lang/String;)V"))
//                }
//                log('=========================================================OVER==================================================================\n\n')
            }
        }

        /**
         * 插入模板方法(获取所有的模板类及其对应的所有方法，并全部执行)
         */
        private void insertTemplate() {
            if (methodMapList.size() == 0 || !excludeMethod) return
            for (Map.Entry<String, List<Map<String, String>>> entry : methodMapList.entrySet()) {
                if (null == entry || null == entry.value || entry.value.size() == 0) return
                entry.value.each {
                    Map<String, String> ms ->
                        String name = ms.get("name")
                        String type = ms.get("type")
                        String desc = ms.get("desc")
                        invokeStatic(Type.getType(type), new Method(name, desc))
                        log("正在给${methodName}插桩......插入的方法是：$name")
                }
            }
        }
    }

    /**
     * 排除一些方法（这些方法不参与插桩）
     * toString  copy  hashCode  equals component1
     * @param name
     * @return
     */
    static boolean excludeMethod(String name) {
        return name != "toString" && name != "copy" && name != "hashCode" && name != "component1" && name != "<clinit>" && !name.contains("\$") && !name.contains("run") && !name.contains("handleMessage") && !name.contains("post") && !name.contains("postDelayed") && !name.contains("getView") && !name.contains("convert") && !name.contains("onBindViewHolder") && !name.contains("getItemViewType") && !name.contains("onCreateViewHolder")
    }

    /**
     * 不参与插桩模板方法
     * toString  copy  hashCode  equals component1
     * @param name
     * @return
     */
    static boolean excludeMethodTemp(String name) {
        return name != "<init>" && name != "toString" && name != "copy" && name != "hashCode" && name != "component1" && name != "<clinit>" && !name.contains("\$")
    }

    /**
     * 打印日志
     */
    static def log(String msg) {
        if (openLog) {
            System.out.println(msg)
        }
    }

}