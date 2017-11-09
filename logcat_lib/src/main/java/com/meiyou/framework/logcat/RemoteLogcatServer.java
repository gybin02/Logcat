package com.meiyou.framework.logcat;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.util.Pair;
import android.text.TextUtils;
import android.util.Log;

import com.meiyou.framework.logcat.remotelogcat.R;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.URLDecoder;
import java.util.Calendar;

/**
 * 日志服务器
 *
 * @author zhengxiaobin@xiaoyouzi.com
 * @since 17/11/8 下午5:50
 */
public class RemoteLogcatServer implements Runnable {

    //Constants
    private static final String REMOTE_LOG_CAT_SERVER_TAG = "RemoteLogcatServer";
    private static final String MILLISECONDS_TO_RELOADING = "#MILLISECONDS_TO_RELOADING#";
    private static final String FILTERED_STRING = "#FILTERED_STRING#";
    private static final String APP_NAME = "#APP_NAME#";
    private static final String LOGCAT_CONTENT_TAG = "#LOGCAT_CONTENT_TAG#";
    private static final String CLEAR_LOG_TAG = "CLEANED_";
    private static final String GENERIC_TAG = "#TAG#";
    private static final String UTF8Encoding = "UTF-8";

    enum FilterTypes {
        TAG_FILTER_START,
        TAG_FILTER_CONTAINS,
        NO_FILTER;
    }

    // Filter constant strings
    private static final String TAG_FILTER_START = "?filterStart=";
    private static final String TAG_FILTER_CONTAINS = "?filterContains=";
    private String filtered_string_base = "";

    //HTML pages feeders
    private String HTML_PAGE_WELCOME = "";
    private String HTML_PAGE_LOGCAT_BASE_CONTENT = "";

    //Operation vars
    private String lastCleanFlag = "";
    private final int mPort;
    private boolean mIsRunning;
    private ServerSocket mServerSocket;

    /**
     * 创建服务器
     *
     * @param port                     //port to open connection      服务器端口
     * @param millisecondsToReloading， 自动刷新页面时间 //page reload rate (ms)
     * @param context
     */
    public RemoteLogcatServer(int port, int millisecondsToReloading, Context context) {
        mPort = port;

        if (context != null && context.getApplicationInfo() != null && context.getApplicationInfo()
                                                                              .loadLabel(context.getPackageManager()) != null) {
            configHTMLFiles(context, millisecondsToReloading);
//            showIPAddressMessage(context);
        }
    }

    /**
     * 读取HTML
     *
     * @param context
     * @param rawResourceId
     * @return
     */
    private String getHTMLPage(Context context, String rawResourceId) {
        try {
            return readTextFile(context.getAssets().open(rawResourceId));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String readTextFile(InputStream inputStream) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, len);
            }
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            Log.d("RemoteLogcatFileError", e.getMessage());
        }
        return outputStream.toString();
    }

    /**
     * 获取IP地址
     *
     * @param context
     * @return
     */
    public String getIP(Context context) {
        final String ipAddress = NetworkUtils.getDeviceIpAddress(context);
        //not avaliable
        String result = "";
        if (ipAddress != null && !ipAddress.isEmpty()) {
            result = "http://" + ipAddress + ":" + getPort();
        }
        return result;
    }

    /**
     * 显示 IP地址在通知栏
     *
     * @param context
     */
    private void showIPAddressMessage(final Context context) {
        final String ipAddress = NetworkUtils.getDeviceIpAddress(context);
        if (ipAddress != null && !ipAddress.isEmpty()) {

            //Open WebBrowser intent
            String url = "http://localhost:" + getPort();
            Intent webBrowserIntent = new Intent(Intent.ACTION_VIEW);
            webBrowserIntent.setData(Uri.parse(url));

            //Build pending intent
            PendingIntent resultPendingIntent =
                    PendingIntent.getActivity(
                            context,
                            0,
                            webBrowserIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );

            //Build notification
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(android.R.drawable.sym_def_app_icon)
                            .setContentTitle(context.getString(R.string.title_notification))
                            .setContentText(context.getString(R.string.notification_message) + ipAddress + ":" + getPort())
                            .setContentIntent(resultPendingIntent);

            //Send notification
            ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(1, mBuilder
                    .build());

        }
    }

    private void configHTMLFiles(Context context, int milliseconds) {

        //App Name
        String strAppName = context.getApplicationInfo()
                                   .loadLabel(context.getPackageManager())
                                   .toString();

        //Getting HTML Files: From raw resources
        HTML_PAGE_WELCOME = getHTMLPage(context, "welcome.html");
        HTML_PAGE_LOGCAT_BASE_CONTENT = getHTMLPage(context, "logcatcontent.html");

        //Formatting HTML Files
        HTML_PAGE_WELCOME = HTML_PAGE_WELCOME.replace(APP_NAME, strAppName);
        HTML_PAGE_LOGCAT_BASE_CONTENT = HTML_PAGE_LOGCAT_BASE_CONTENT
                .replace(APP_NAME, strAppName)
                .replace(MILLISECONDS_TO_RELOADING, String.valueOf(milliseconds));
        filtered_string_base = context.getString(R.string.filtered_by_header);
    }

    /**
     * This method starts the web server listening to the specified port.
     */
    public void startServer() {
        mIsRunning = true;
        new Thread(this).start();
    }

    /**
     * This method stops the web server
     */
    public void stopServer() {
        try {
            mIsRunning = false;
            if (null != mServerSocket) {
                mServerSocket.close();
                mServerSocket = null;
            }
        } catch (IOException e) {
            Log.e(REMOTE_LOG_CAT_SERVER_TAG, "Error closing the server socket.", e);
        }
    }

    public boolean isRunning() {
        return mIsRunning;
    }

    public int getPort() {
        return mPort;
    }

    @Override
    public void run() {
        try {
            mServerSocket = new ServerSocket(mPort);
            while (mIsRunning) {
                Socket socket = mServerSocket.accept();
                handleRequest(socket);
                socket.close();
            }
        } catch (SocketException e) {
            // The server was stopped; ignore.
        } catch (IOException e) {
            Log.e(REMOTE_LOG_CAT_SERVER_TAG, "Web server error.", e);
        }
    }

    private void handleRequest(Socket socket) throws IOException {
        BufferedReader reader = null;
        PrintStream output = null;
        try {
            String query = null;

            // Read HTTP headers and parse out the query.
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line;
            Boolean forcePreviousCleaning = false;

//            while 
            if (!TextUtils.isEmpty(line = reader.readLine())) {
                //supported API requests
                if (line.startsWith("GET /log")) {
                    query = getQueryString(line);
//                    break;
                }
                if (line.startsWith("POST /log")) {
                    //HACK: All POST queries clean Logcat
                    query = getQueryString(line);
                    forcePreviousCleaning = true;
//                    break;
                }
            }

            // Output stream that we send the response to
            output = new PrintStream(socket.getOutputStream());

            // cath the query.
            if (null == query) {
                createUnknownRequestResponse(output);
                return;
            }
            byte[] bytes = readLogcatContent(query, forcePreviousCleaning);
            if (null == bytes) {
                //we dont understand this request
                createUnknownRequestResponse(output);
                return;
            }

            //Ok response
            createOkResponse(output, bytes);

        } finally {
            if (null != output) {
                output.close();
            }
            if (reader != null) {
                reader.close();
            }
        }
    }

    @NonNull
    private String getQueryString(String line) {
        String query;
        int start = line.indexOf('/') + 4;
        int end = line.indexOf(' ', start);
        query = line.substring(start, end);
        try {
            return URLDecoder.decode(query, UTF8Encoding).trim();
        } catch (Exception e) {
            return query;
        }

    }

    private void createOkResponse(PrintStream output, byte[] bytes) throws IOException {
        // formatting response
        output.println("HTTP/1.0 200 OK");
        output.println("Content-Type: text/html");
        output.println("Content-Length: " + bytes.length);
        output.println();
        output.write(bytes);
        output.flush();
    }

    /**
     * Send welcome page when an unknown request has been made
     */
    private void createUnknownRequestResponse(PrintStream output) {

        byte[] bytes = null;
        try {
            bytes = readBadRequestContent();
        } catch (IOException e) {
            e.printStackTrace();
        }

        output.println("HTTP/1.0 400 Bad Request");
        output.println("Content-Type: text/html");
        if (bytes != null) {
            output.println("Content-Length: " + bytes.length);
            output.println();
            try {
                output.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        output.flush();
    }


    /**
     * The content of the Logcat.
     */
    private byte[] readLogcatContent(String route, boolean previousCleaning) throws IOException {

        try {

            Pair<FilterTypes, String> filter = getFilter(route);
            String BASE_HTML_CONTENT = "";
            if (filter.first.equals(FilterTypes.NO_FILTER)) {
                BASE_HTML_CONTENT = HTML_PAGE_LOGCAT_BASE_CONTENT
                        .replace(FILTERED_STRING, "");
            } else {
                BASE_HTML_CONTENT = HTML_PAGE_LOGCAT_BASE_CONTENT
                        .replace(FILTERED_STRING, filtered_string_base.replace(GENERIC_TAG, filter.second));
            }

            if (previousCleaning) {
                if ( //Only for Rooted devices => result=0
                        Runtime.getRuntime().exec("logcat -c").waitFor() != 0
                        ) {

                    //Workaround: Your device is no rooted
                    Log.d("CLEAN_LOG_FLAG", CLEAR_LOG_TAG + String.valueOf(Calendar.getInstance()
                                                                                   .getTimeInMillis()));
                    lastCleanFlag = CLEAR_LOG_TAG + String.valueOf(Calendar.getInstance()
                                                                           .getTimeInMillis());
                }
                ;
            }

            Process process = Runtime.getRuntime()
                                     .exec("logcat -d -v time"); //forceing the "tag" mode, otherwise, mode depends from device
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            StringBuilder log = new StringBuilder();
            String line = "";
            boolean cleanFlagReached = false;

            int lineNumber = 0;
            //Load logcat content
            while ((line = bufferedReader.readLine()) != null) {

                // To filter last clean
                if (!lastCleanFlag.equals("") && line.contains(lastCleanFlag)) {
                    cleanFlagReached = true;
                    continue;
                } else if (!lastCleanFlag.equals("") && !cleanFlagReached) {
                    continue;
                }

                switch (filter.first) {
                    case TAG_FILTER_START:
                        if (line.startsWith(filter.second)) {
                            processLine(log, line, lineNumber);
                        }
                        break;
                    case TAG_FILTER_CONTAINS:
                        if (line.contains(filter.second)) {
                            processLine(log, line, lineNumber);
                        }
                        break;
                    default:
                        processLine(log, line, lineNumber);
                }
                lineNumber++;
            }
            return getBytesFromString(BASE_HTML_CONTENT.replace(LOGCAT_CONTENT_TAG, log.toString()));

        } catch (Exception ex) {
            Log.e("RemoteLogcatError", ex.getMessage());
            return null;
        }
    }

    private String getString(InputStream inputStream) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder total = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) {
            total.append(line).append('\n');
        }
        return total.toString();
    }

    private void processLine(StringBuilder log, String line, int lineNumber) {
        log.append(HighLighting.applyHighLighting(line, lineNumber));
//        .append("<br/>")
    }


    /**
     * When bad request performs server returns Welcome
     */
    private byte[] readBadRequestContent() throws IOException {
        InputStream input = null;
        try {
            return getBytesFromString(HTML_PAGE_WELCOME);
        } catch (FileNotFoundException e) {
            return null;
        } finally {
            if (null != input) {
                input.close();
            }
        }
    }

    private byte[] getBytesFromString(String inputString) throws IOException {

        InputStream inputData = new ByteArrayInputStream(inputString.getBytes(UTF8Encoding));
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        int size;
        while (-1 != (size = inputData.read(buffer))) {
            output.write(buffer, 0, size);
        }
        output.flush();
        return output.toByteArray();
    }


    private Pair<FilterTypes, String> getFilter(String route) {

        if (route.contains(TAG_FILTER_START)) {
            return new Pair<>(FilterTypes.TAG_FILTER_START, route.replace(TAG_FILTER_START, ""));

        } else if (route.contains(TAG_FILTER_CONTAINS)) {
            return new Pair<>(FilterTypes.TAG_FILTER_CONTAINS, route.replace(TAG_FILTER_CONTAINS, ""));
        }
        return new Pair<>(FilterTypes.NO_FILTER, "");
    }


}
