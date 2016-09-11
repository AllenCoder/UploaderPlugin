package com.pre.im;

import groovy.json.JsonSlurper
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

/**
 *
 */
public class PrePlugin implements Plugin<Project> {
    private Project project = null;

    // URL for uploading apk file
    private static final String APK_UPLOAD_URL = "http://api.pre.im/api/v1/app/upload";


    @Override
    void apply(Project project) {
        this.project = project
        // 接收外部参数
        project.extensions.create("pre", PreExtension)

        // 取得外部参数
        if (project.android.hasProperty("applicationVariants")) { // For android application.
            project.android.applicationVariants.all { variant ->
                String variantName = variant.name.capitalize()

                // Check for execution
                if (false == project.pre.enable) {
                    project.logger.error("Pre.im gradle enable is false, if you want to auto upload apk file, you should set the execute = true")
                    return
                }

                // Create task.
                Task preTask = createUploadTask(variant)

                project.tasks["assemble${variantName}"].doLast {
//                     if debug model and debugOn = false no execute upload
                    if (variantName.contains("Debug") && !project.pre.debugOn) {
                        println("Pre: the option debugOn is closed, if you want to upload apk file on debug model, you can set debugOn = true to open it")
                        return
                    }

                    if (variantName.contains("Release")) {
                        println("Pre.in: the option autoUpload is opened, it will auto upload the release to the pre platform")
                    }
                    uploadApk(generateUploadInfo(variant))

                }
            }
        }
    }

    /**
     * generate upload info
     * @param variant
     * @return
     */
    public UploadInfo generateUploadInfo(Object variant) {
        def manifestFile = variant.outputs.processManifest.manifestOutputFile[0]
        println("-> Manifest: " + manifestFile)
        println("VersionCode: " + variant.getVersionCode() + " VersionName: " + variant.getVersionName())

        UploadInfo uploadInfo = new UploadInfo()
        uploadInfo.user_key = project.pre.user_key;

        uploadInfo.password = project.pre.password;
        uploadInfo.update_notify =project.pre.update_notify;
        // if you not set apkFile, default get the assemble output file
        if (project.pre.file != null) {
            uploadInfo.file = project.pre.file
            println("pre: you has set the custom file")
            println("pre: your apk absolutepath :" + project.pre.file)
        }else {
            File apkFile = variant.outputs[0].outputFile
            uploadInfo.file = apkFile.getAbsolutePath()
            println("pre: the apkFile is default set to build file")
            println("pre: your apk absolutepath :" + apkFile.getAbsolutePath())
        }


        return uploadInfo
    }

    /**
     * 创建上传任务
     *
     * @param variant 编译参数
     * @return
     */
    private Task createUploadTask(Object variant) {
        String variantName = variant.name.capitalize()
        Task uploadTask = project.tasks.create("upload${variantName}PreApkFile") << {
            // if debug model and debugOn = false no execute upload
            if (variantName.contains("Debug") && !project.pre.debugOn) {
                println("Pre: the option debugOn is closed, if you want to upload apk file on debug model, you can set debugOn = true to open it")
                return
            }
            uploadApk(generateUploadInfo(variant))
        }
        println("Pre:create upload${variantName}PreApkFile task")
        return uploadTask
    }

    /**
     *  上传apk
     * @param uploadInfo
     * @return
     */
    public boolean uploadApk(UploadInfo uploadInfo) {
        // 拼接url如：curl -F "file=@abc.apk" -F "user_key={user_key}" -F "update_notify=1" http://api.pre.im/api/v1/app/upload
        String url = APK_UPLOAD_URL
        println("Pre.im: Apk start uploading....")
//        //
        if (uploadInfo.user_key == null) {
            project.logger.error("Please set the user_key = \"xxxxx900037672xxx\"")
            return false
        }
        println("Pre:" + uploadInfo.toString())

        if (!post(url, uploadInfo.file, uploadInfo)) {
            project.logger.error("Pre: Failed to upload!")
            return false
        } else {
            println("Pre: upload apk success !!!")
            return true
        }
    }

    /**
     * 上传apk
     * @param url 地址
     * @param filePath 文件路径
     * @param uploadInfo 更新信息
     * @return
     */
    public boolean post(String url, String filePath, UploadInfo uploadInfo) {
        HttpURLConnectionUtil connectionUtil = new HttpURLConnectionUtil(url, Constants.HTTPMETHOD_POST);
        connectionUtil.addTextParameter(Constants.USER_KEY,uploadInfo.user_key);
        connectionUtil.addTextParameter(Constants.PASSWORD,uploadInfo.password);
        connectionUtil.addTextParameter(Constants.UPDATE_NOTIFY,uploadInfo.update_notify);

        connectionUtil.addFileParameter(Constants.FILE, new File(filePath));
        String result = new String(connectionUtil.post(), "UTF-8");
        println("Pre ---result: " + result)
        def data = new JsonSlurper().parseText(result)
        if (data.code=="0") {
            println("Pre --->update success: " + data.msg)
            return true
        }
        return false;
    }



}
