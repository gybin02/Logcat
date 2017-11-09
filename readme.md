## LogCat
![logcat.jpg](/img/logcat.jpg)
一个在APP内建立Socket服务器提供数据，同时在浏览器上直接查看安卓 LogCat的工具库；不需要USB连接手机，就可查看Log（安卓有些手机连接电脑特繁琐）。 方便QA，不用安装 Android Studio 就能查看LOG; 错误日志实时查看等；

### 如何使用?
  1. Gradle 集成
 ```groovy
    debugCompile 'com.meiyou.framework:logcat:1.0.0-SNAPSHOT'
    releaseCompile 'com.meiyou.framework:logcat-noop:1.0.0-SNAPSHOT'
 ```
 2. 代码开启服务器功能；
 ```java
       LogCatHelper.getInstance().startServer();
       //获取访问地址
       //LogCatHelper.getInstance().getIp();
 ```
 3. 使用浏览器访问：eg: `http://192.168.0.128:8080`
 4. 模拟器使用： 
 
    ```
        adb forward tcp:8080 tcp:8080
        # 把PC端8000端口的数据, 转发到Android端的9000端口上.
        #   adb forward tcp:8000 tcp:9000
    ```
### 截图
![screen](/img/screencapture.png)  
    
### 功能
  1. 使用浏览器查看LogCat
  2. 支持Tag过滤；
  3. 支持自动刷新，停止刷新
  4. 支持清除Log.
  5. JSON格式数据 高亮显示
  6. OKHTTP 请求，返回值加样式显示
  
