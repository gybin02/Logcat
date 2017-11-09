package com.meiyou.framework;

import android.content.Context;
import android.support.annotation.NonNull;

import com.meiyou.framework.logcat.RemoteLogcatServer;

/**
 * 主入口类
 *
 * @author zhengxiaobin@xiaoyouzi.com
 * @since 17/11/9
 */

public class LogCatHelper {
    private static LogCatHelper instance;
    private RemoteLogcatServer logcatServer;

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
        startServer(context, new LogCatConfig());
    }
    
    
    public void startServer(@NonNull Context context, @NonNull LogCatConfig config) {
        Context applicationContext = context.getApplicationContext();
        logcatServer
                = new RemoteLogcatServer(
                config.port,
                config.millisecondsToReloading,
                applicationContext
        );

        logcatServer.startServer();
    }

    /**
     * 关闭服务器
     */
    public void stopServer() {
        logcatServer.stopServer();
    }

    public boolean isServerRunning() {
        return logcatServer.isRunning();
    }

    /**
     * 获取服务器IP
     *
     * @param context
     * @return
     */
    public String getServerIp(Context context) {
        return logcatServer.getIP(context);
    }
}