## LogCat
![logcat.jpg](/img/logcat.jpg)
- 一个在APP内建立Socket服务器提供Logcat数据，同时在浏览器上直接查看LogCat的工具库；
- 不需要USB连接手机，就可查看Log（安卓有些手机连接电脑特繁琐）。 方便QA，不用安装 Android Studio 就能查看LOG; 
- 错误日志实时查看等；

### 问题：
 - Qa 大部分并没有安装 AndroidStudio，如果APP出现异常，只能通过界面描述来表述问题；缺少必要的LogCat信息，对开发来说，发现和调试问题是比较 麻烦的步骤；
 - 经期会把异常后的LogCat保存在SD卡，但是连接 电脑导出log文件，然后分析错误位置，截图发给开发等等步骤都挺繁琐，消耗时间。
 -  本工具可以实现，实时，不间断在Web browser里打印出LogCat，QA发现app问题，可以马上截图，获取上下文Log，发送给QA。极大节省了QA的时间和极大方便了开发者排查问题。
 
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
 3. 使用浏览器访问：eg: `http://192.168.0.128:8080`； 有接入[Developer小火箭](http://git.meiyou.im/Android/developer)的，可以在调试界面看到访问地址; 同时相应的地址也会在LogCat打印出。
 
 4. 模拟器使用： 
 
    ```
        adb forward tcp:8080 tcp:8080
        # 把PC端8000端口的数据, 转发到Android端的9000端口上.
        #   adb forward tcp:8000 tcp:9000
    ```
### 截图
<img src="/Android/LogCat/raw/master/img/screencapture.png"  style="width:800px">
    
### 功能TODO
  1. 使用浏览器查看LogCat
  2. 支持Tag过滤；
  3. 支持自动刷新，停止刷新
  4. 支持清除Log.
  5. JSON格式数据 高亮显示
  6. OKHTTP 请求，返回值加样式显示
  7. 网页前台修改，使用Ajax发起循环请求，
  8. 新增 不同 Log 标准 过滤
  
### 引用
- [adb logcat 命令行用法](http://blog.csdn.net/tumuzhuanjia/article/details/39555445)
- [使用NanoHttpd在Android上实现HttpServer](http://blog.csdn.net/kingroc/article/details/52275741)
