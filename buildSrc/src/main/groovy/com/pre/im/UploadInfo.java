package com.pre.im;

/**
 * 作者: allen on 16/9/9.
 */

public class UploadInfo {
    /**
     *
     */
    public String user_key = null            ;
    /**
     * 需要上传的ipa或者apk文件
     */
    public String file = null                ;
    /**
     * 下载密码 (传空字符串或不传则不设置密码)
      */
    public String password = null             ;
    /**
     * 向内测成员发布新版通知 (1:开启)
     */
    public String update_notify = null       ;

    @Override
    public String toString() {
        return "UploadInfo{" +
                "user_key='" + user_key + '\'' +
                ", file='" + file + '\'' +
                ", password='" + password + '\'' +
                ", update_notify='" + update_notify + '\'' +
                '}';
    }
}
