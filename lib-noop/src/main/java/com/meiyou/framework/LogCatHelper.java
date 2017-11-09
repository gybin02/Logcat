package com.meiyou.framework;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;


/**
 * 主入口类
 *
 * @author zhengxiaobin@xiaoyouzi.com
 * @since 17/11/9
 */

public class LogCatHelper {
    private static LogCatHelper instance;

    private static final String TAG = "LogCatHelper";

    public static LogCatHelper getInstance() {
        if (instance == null) {
            instance = new LogCatHelper();
        }
        return instance;
    }

    /**
     * 启动服务器
     *
     * @param context
     */
    public void startServer(@NonNull Context context) {
        Log.d(TAG, "startServer: ");
    }


    public void startServer(@NonNull Context context, @NonNull LogCatConfig config) {
        Log.d(TAG, "startServer: config");
    }

    /**
     * 关闭服务器
     */
    public void stopServer() {
        Log.d(TAG, "stopServer: ");
    }

    public boolean isServerRunning() {
        Log.d(TAG, "isServerRunning: ");
        return false;
    }

    /**
     * 获取服务器IP
     *
     * @param context
     * @return
     */
    public String getServerIp(Context context) {
        Log.d(TAG, "getServerIp: ");
        return "";
    }
}
