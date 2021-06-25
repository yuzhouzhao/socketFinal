Socket 大作业报告

- 组号：15
- 组长：191250215 周子杰
- 组员：
  - 191250165 徐琪
  - 191250162 熊智星
  - 191250204 赵宇舟
  - 191250113 蒲中正

[TOC]

## 1-大作业概述

### 1.1 大作业主题

主题：socket编程-主题1-基于Java Socket API搭建简单的HTTP客户端和服务器端程序

### 1.2 主题说明

1. 不允许基于netty等框架，完全基于Java Socket API进行编写

2. 不分区使用的IO模型，BIO、NIO和AIO都可以

3. 实现基础的HTTP请求、响应功能，具体要求如下：

     3.1 HTTP客户端可以发送请求报文、呈现响应报文（命令行和GUI都可以）

     3.2 HTTP客户端对301、302、304的状态码做相应的处理

     3.3 HTTP服务器端支持GET和POST请求

     3.4 HTTP服务器端支持200、301、302、304、404、405、500的状态码

     3.5 HTTP服务器端实现长连接

     3.6 MIME至少支持三种类型，包含一种非文本类型

4. 基于以上的要求，实现注册，登录功能

### 1.3 使用语言

使用语言：Java

### 1.4 运行方式

运行方式：在IntelliJ IDEA中同时运行客户端与服务器端，按指定格式与提示发送请求

## 2-项目详解

### 2.1-服务端

#### 2.1.0-Server and ServerHandler

- 在服务端，我们有两个类 Server 和 ServerHandler
- Server 类负责和客户端建立连接，接收 request 报文，调用 ServerHandler 类处理报文，以及向客户端发送 response 报文
- ServerHandler 类负责HTTP协议的具体处理实现

#### 2.1.1-Server 类

**功能**：建立连接，接收请求报文，调用 ServerHandler 类处理报文，以及应答发送报文。

**代码结构**：

- 建立server并监听端口
- 从 InputStream 中获取 request 报文
- 调用 ServerHandler 类解析报文、处理
- 将 response 报文写入 OutputStream

#### 2.1.2-ServerHandler 类

**功能**：

- HTTP协议的具体处理实现，先是解析 request 报文，然后按照不同的方法进行处理
- 我们实现的方法有：GET、POST（支持登陆和注册功能）；
- 我们实现的状态码有：200、301、302、304、404、405、500
- 我们实现的MIME类型有：txt，html，jpg，其中jpg为非文本类型

**代码结构**：

- 初始化 ServerHandler 时解析报文，获取首行、头部、主体后初始化一个 HttpRequest 类型的 request 报文
- 按照不同的方法进行处理
- 根据指令处理的不同情况，按不同状态码处理
- 处理结束后，初始化一个  HttpResponse 类型的 response 报文

**ServerHandler 类的关键数据结构**：

```java
public class ServerHandler {
    private HttpRequest httpRequest;//request 报文
    private HttpResponse httpResponse;//response 报文

    private Map<Integer, String> codeAndMessage = new HashMap<>();// 状态码和对应message
    private Map<String, String> mimeType = new HashMap<>();// MIME类型
    private Map<String, String> reversedMime = new HashMap<>();// 反向查找MIME类型

    // {文件名，状态(valid, deleted, temp)}
    private Map<String, String> fileStatus = new HashMap<>();
    // {文件名，路径}
    private Map<String, String> filePath = new HashMap<>();
    // {文件名，临时文件名}
    private Map<String, String> tempFile = new HashMap<>();

    //储存注册的账户
    private static Map<String, String> Account = new HashMap<>();
    //储存已经登录的用户的cookie和对应的用户
    private static Map<String, String> CookieToAccount = new HashMap<>();
    //储存已登录的用户和对应的cookie
    private static Map<String, String> AccountToCookie = new HashMap<>();
    private static int LoggedUser = 0;//储存已经登录的用户数目
}
```

**ServerHandler 类的关键方法**：

```java
public class ServerHandler {
	// ServerHandler 的构造方法
    // 从请求报文 data 中获取 startLine、head 和 body
    public ServerHandler(byte[] data) throws IOException;

    // 处理指令，获得响应报文 1）GET 方法 2）POST 方法 3）其他的方法不支持，返回状态码405
    public HttpResponse process();

    // 处理GET方法
    private void doGet();

    // 处理POST方法，包括登陆与注册
    private void doPost();

    // 处理 200 OK
    private void do200(byte[] body, String type);

    // 处理 301 Moved Permanently
    private void do301(String newPath);

    // 处理 302 Found
    private void do302(String tempPath);

    // 处理 304 Not Modified
    private void do304();

    // 处理 404 Not Found
    private void do404();

    // 处理 405 Method Not Allowed
    private void do405();

    // 处理 500 Internal Server Error
    private void do500();
    
    //处理注册请求
    private void register();
    
    //处理登陆请求
    private void login();
}
```

### 2.2-客户端

#### 2.2.0-Client 、ClientHandler、RequestHandler、LoginUI and RegisterUI

- 在客户端，我们有五个类Client、 ClientHandler、RequestHandle、LoginUI and RegisterUI
- Client 类负责和服务端建立连接，发送request报文，接受服务端发送的response报文，调用ClientHandler和RequestHandler类处理报文
- ClientHandler 类负责解析request和response报文、处理mime类型、处理状态码301、302、304
- RequestHandler 类负责根据用户输入进行 GET和POST request请求处理控制台信息
- LoginUI 类负责登录界面的用户交互，调用Client类发送request报文
- RegisterUI类负责注册界面的用户交互，调用Client类发送request报文

#### 2.2.1-Client类

**功能：**与服务端进行连接，发送request报文，接受服务端发送的response报文，调用ClientHandler和RequestHandler类处理报文

**代码结构：**

- 建立client并与服务端连接
- 判断报文数据通过控制台获取或通过UI界面获取
- 通过OutputStream发送request报文，InputStream中获取response报文
- 调用RequestHandler处理控制台发送的request请求，并发送给服务端
- 监听并调用ClientHandler处理服务端发来的response报文

**Client类的关键数据结构和方法：**

```java
public class Client extends Thread{
    private static Socket client=null;//客户端
    private static Map<String,String> fileMap;//已知服务器的映射    
    //启动客户端
    public static void main(String[] args) throws InterruptedException;
    
    //建立client
    public Client(int port); 
    
    //运行client
    public static void run(HttpRequest httpRequest);
    
    //初始化fileMap
    public static void setFileMap();
}
```

```java
class Client_heart implements Runnable {
    private Socket socket;
    private OutputStream os;
    private Map<String,String> fileMap=new HashMap<>();
    Client_heart(Socket socket, OutputStream oos);
    public void run();
}
```

#### 2.2.2-ClientHandler类

**功能：**解析request和response报文、处理mime类型、处理状态码301、302、304

**代码结构：**

- 根据结构解析request报文
- 根据结构解析response报文
- 根据状态码做出客户端回应

**ClientHandler类关键数据结构和方法：**

```java
public class ClientHandler {
    private static String method; //请求报文的方法
    private static String url;  //请求报文的路径
    private static byte[] message_body;  //请求报文的body
    private HttpRequest httpRequest; //request报文
    private HttpResponse httpResponse; //response报文
    private String newUrl=""; // 新的路径(301 302)
   	private static Map<String, String> mimes = new HashMap<>(); //mime类型映射
    
    //解析request和respone报文
    public ClientHandler(byte[] requestData,byte[] responseData) throws IOException;
    
    //对状态码做出回应，传给client
    public int response();
    
    //打开目标文件
    public void show(String fileName);
    
    //对不同的状态码做出回应
    private int do200();
    public byte[] do301();
    public byte[] do302();
    private int do304();
}
```

#### 2.2.3-RequestHandler类

**功能：**根据用户输入进行 GET和POST request请求处理控制台信息，调用HttpRequest将控制台信息整合成报文

**代码结构：**

- 根据用户输入判断请求类型（GET/POST)
- 将控制台输入的信息整理并调用HttpRequest进行报文合成

**RequestHandler类关键数据结构和方法：**

```java
public class RequestHandler {
    private HttpRequest httpRequest;//request报文
    private String FileName_Suffix="";//带扩展后缀的文件名
    //处理POST请求
    void handlePost();
    
    //处理GET请求
    void handleGet(Map<String,String> fileMap);
    
    //根据输入判断请求类型并调用相关函数进行处理
    RequestHandler(Map<String,String> fileMap,HttpRequest httpReq);
}
```

#### 2.2.4-Client类线程内部类-Client_heart

**功能：**HTTP1.1默认实现长连接，因此采用HTTP1.1后项目长连接默认实现。但为显式实现客户端向服务器端的长连接，将长连接可视化，我们在client类中新开一个线程专门朝服务器端定时发送POST请求，模仿人类心跳，客户端每隔五秒通过向服务器端发送“心跳包”报文确认是否能继续访问服务器，并打印访问成功的相应访问报文，以实现长连接。

**代码结构：**

- 继承Tread并重写run方法

```java
class Client_heart implements Runnable {
    private Socket socket;
    private OutputStream os;
    private Map<String,String> fileMap=new HashMap<>();
    
    //采用和主线程同一套socket与输入输出流
    Client_heart(Socket socket, OutputStream oos){
        this.socket = socket;
        this.os = oos;
    }
    //实现心跳包post报文的封装发送，并打印成功访问服务器端的响应报文。每隔五秒进行一次。
    @Override
    public void run();
```

#### 2.2.5-LoginUI类

**功能：**与登录界面的用户交互，调用Client类发送request报文

**代码结构：**

- 监听获取账号密码输入
- 封装账号密码输入为request报文
- 调用Client类发送request报文

**LoginUI类的关键方法实现：**

```java
//封装获取到的账号密码为request报文并调用Client类发送报文
String startLine = "POST /login HTTP/1.1";
Map<String,String> headers=new HashMap<>();
String bodyStr="username="+username+"&password="+password;
headers.put("Content-type","text/plain");
headers.put("Content-length", 
String.valueOf(bodyStr.getBytes().length));
HttpRequest httpRequest=new HttpRequest(startLine,headers,bodyStr.getBytes());
Client.run(httpRequest);
```

#### 2.2.6-RegisterUI类

**功能：**与注册界面的用户交互，调用Client类发送request报文

**代码结构：**

- 监听获取账号密码输入
- 封装账号密码输入为request报文
- 调用Client类发送request报文

**RegisterUI类的关键方法实现：**

```java
//封装获取到的账号密码为request报文并调用Client类发送报文
String startLine = "POST /register HTTP/1.1";
Map<String,String> headers=new HashMap<>();
String bodyStr="username="+name+"&password="+passwd+"&confrimpassword"+confrimpasswd;
headers.put("Content-type","text/plain");
headers.put("Content-length", String.valueOf(bodyStr.getBytes().length));
HttpRequest httpRequest=new HttpRequest(startLine,headers,bodyStr.getBytes());
Client.run(httpRequest);
```

### 2.3-HTTP报文封装

#### 2.3.0-HttpObject、HttpRequest and HttpResponse

- HTTP包进行报文格式处理封装，方便报文在客户端和服务端之间传递，包含三个类，HttpObject、HttpRequest and HttpResponse
- HttpObject为报文父类，封装了报文各个结构的get、set方法
- HttpRequest 类继承了HttpObject，根据request报文首行解析出方法，文件路径和http版本和设定请求行、请求体
- HttpResponse 类继承了HttpObject，根据response报文首行解析出http版本、状态码和说明和设定响应头和响应数据

#### 2.3.1-HttpObject类

**功能：**进行报文格式处理封装，方便报文在客户端和服务端之间传递，包含三个类，HttpObject、HttpRequest and HttpResponse

**代码结构：**

- 报文各部分的get、set方法封装
- 报文首行、请求行/响应头部、空白行和请求体/响应数据的整合

**HttpObject类的关键数据结构和方法：**

```java
public class HttpObject {
    protected String startLine;//报文首行
    protected int headerCount;//报文请求行//响应头部数
    //返回报文首行
    public String getStartLine();
	//设置报文首行
    public void setStartLine(String startLine);
	//返回报文请求行/响应头部数目
    public int getHeaderCount();
	//设置报文请求/响应头部行数目
    public void setHeaderCount(int headerCount) ;
	//添加报文请求行//响应头部
    public void addHeaders(String name, String value);
    //根据key值返回具体的请求行//响应头部
    public String getHeader(String name);
    //返回报文请求体/响应数据
    public byte[] getBody();
	//设置报文请求体/响应数据
    public void setBody(byte[] body);
    protected Map<String, String> headers; //报文请求行/响应头部映射
    protected byte[] body;//报文请求体/响应数据
    //将首行和请求行/响应头部转成string（用于输出）
    public String startLineAndHeadersToString();
}
```

#### 2.3.2-HttpRequest类

**功能：**继承了HttpObject，根据request报文首行解析出方法，文件路径和http版本

**代码结构：**

- 根据request报文首行解析出方法，文件路径和http版本和设定请求行、请求体
- 返回request报文的方法、文件路径和http版本
- 判断客户端和服务端的连接状态

**HttpRequest类的关键数据结构和方法：**

```java
public class HttpRequest extends HttpObject {
    private String method; // 请求方法
    private String url; //文件路径
    private String version; //http版本
	//根据request报文首行解析出方法，文件路径和http版本和设定请求行、请求体
    public HttpRequest(String startLine, Map<String, String> headers, byte[] body);
    //返回请求方法
    public String getMethod();
	//返回文件路径
    public String getUrl();
	//返回http版本
    public String getVersion();
	//返回请求行
    public Map<String,String> getHeaders();
	//判断连接状态
    public boolean getConnectionState();
}
```

#### 2.3.3-HttpResponse类

**功能：**根据response首行解析出http版本、状态码和说明

**代码结构：**

- 根据response首行解析出http版本、状态码和说明和设定响应头和响应数据
- 返回状态码、http版本和响应头部

**HttpResponse类的关键数据结构和方法：**

```java
public class HttpResponse extends HttpObject{
    private String version; //http版本
    private int stateCode; //状态码
    private String reason; //说明
	//根据response首行解析出http版本、状态码和说明和设定响应头和响应数据
    public HttpResponse(String startLine, Map<String, String> headers, byte[] body);
    //返回http版本
    public String getVersion();
    //返回状态码
    public int getStateCode();
    //返回响应头部
    public Map<String,String> getHeaders();
```

## 3-功能展示

### 3.1-登录

#### 3.1.1-登陆未注册的账号

- UI：

  ![](https://socket-runtime-pics.oss-cn-shanghai.aliyuncs.com/%E7%99%BB%E5%BD%95%E6%9C%AA%E6%B3%A8%E5%86%8C%E8%B4%A6%E5%8F%B7UI.png)

- 客户端：

  <img src="https://seec2.oss-cn-beijing.aliyuncs.com/登录未注册账号客户端.png?versionId=CAEQDxiBgMDD6bK80RciIDViZTRkMzcyNjVhZjQzOWNhZWYzMjBiNzM4ZTU0MWZj">

- 服务器端：

  ![](https://socket-runtime-pics.oss-cn-shanghai.aliyuncs.com/%E7%99%BB%E5%BD%95%E6%9C%AA%E6%B3%A8%E5%86%8C%E8%B4%A6%E5%8F%B7%E6%9C%8D%E5%8A%A1%E5%99%A8%E7%AB%AF.png)

#### 3.1.2-登录已注册的账号

- UI：

  ![](https://socket-runtime-pics.oss-cn-shanghai.aliyuncs.com/%E7%99%BB%E5%BD%95%E5%B7%B2%E6%B3%A8%E5%86%8C%E8%B4%A6%E5%8F%B7UI.png)

- 客户端：

  ![](https://socket-runtime-pics.oss-cn-shanghai.aliyuncs.com/%E7%99%BB%E5%BD%95%E5%B7%B2%E6%B3%A8%E5%86%8C%E8%B4%A6%E5%8F%B7%E5%AE%A2%E6%88%B7%E7%AB%AF.png)

- 服务器端：

  ![](https://socket-runtime-pics.oss-cn-shanghai.aliyuncs.com/%E7%99%BB%E5%BD%95%E5%B7%B2%E6%B3%A8%E5%86%8C%E8%B4%A6%E5%8F%B7%E6%9C%8D%E5%8A%A1%E5%99%A8%E7%AB%AF.png)

#### 3.1.3-登陆密码错误

* UI：

<img src="https://socket-runtime-pics.oss-cn-shanghai.aliyuncs.com/%E7%99%BB%E5%BD%95%E5%B7%B2%E6%B3%A8%E5%86%8C%E8%B4%A6%E5%8F%B7UI.png">

* 客户端：

<img src="https://seec2.oss-cn-beijing.aliyuncs.com/password_wrong_client.png?versionId=CAEQDxiBgIDQl8e80RciIDE1NzQyNmVmMjkxNTRjZjA5MTgyMTkxM2FkZTdkODhl">

* 服务器端：

<img src="https://seec2.oss-cn-beijing.aliyuncs.com/password_wrong_server.png?versionId=CAEQDxiBgICDmMe80RciIGZhMTljMWQwMzlhOTQ2MmViNDkzZDc2OGQ5ZTUxYmQy">

### 3.2-注册

#### 3.2.1-注册未注册的账号

- UI：

  ![](https://socket-runtime-pics.oss-cn-shanghai.aliyuncs.com/%E6%B3%A8%E5%86%8C%E6%9C%AA%E6%B3%A8%E5%86%8C%E8%B4%A6%E5%8F%B7UI.png)

- 客户端：

  ![](https://socket-runtime-pics.oss-cn-shanghai.aliyuncs.com/%E6%B3%A8%E5%86%8C%E6%9C%AA%E6%B3%A8%E5%86%8C%E8%B4%A6%E5%8F%B7%E5%AE%A2%E6%88%B7%E7%AB%AF.png)

- 服务器端：

  ![](https://socket-runtime-pics.oss-cn-shanghai.aliyuncs.com/%E6%B3%A8%E5%86%8C%E6%9C%AA%E6%B3%A8%E5%86%8C%E8%B4%A6%E5%8F%B7%E6%9C%8D%E5%8A%A1%E5%99%A8%E7%AB%AF.png)

#### 3.2.2-注册已注册的账号

- UI：

  ![](https://socket-runtime-pics.oss-cn-shanghai.aliyuncs.com/%E6%B3%A8%E5%86%8C%E5%B7%B2%E6%B3%A8%E5%86%8C%E8%B4%A6%E5%8F%B7UI.png)

- 客户端：

  ![](https://socket-runtime-pics.oss-cn-shanghai.aliyuncs.com/%E6%B3%A8%E5%86%8C%E5%B7%B2%E6%B3%A8%E5%86%8C%E8%B4%A6%E5%8F%B7%E5%AE%A2%E6%88%B7%E7%AB%AF.png)

- 服务器端：

  ![](https://socket-runtime-pics.oss-cn-shanghai.aliyuncs.com/%E6%B3%A8%E5%86%8C%E5%B7%B2%E6%B3%A8%E5%86%8C%E8%B4%A6%E5%8F%B7%E6%9C%8D%E5%8A%A1%E5%99%A8%E7%AB%AF.png)

### 3.3-简单GET方法

- 客户端

![](https://httpserverandclient.oss-cn-beijing.aliyuncs.com/%E7%AE%80%E5%8D%95get%E6%96%B9%E6%B3%95_%E5%AE%A2%E6%88%B7%E7%AB%AF.png)

- 服务器端

  ![](https://httpserverandclient.oss-cn-beijing.aliyuncs.com/%E7%AE%80%E5%8D%95get%E6%96%B9%E6%B3%95_%E6%9C%8D%E5%8A%A1%E7%AB%AF.png)

### 3.4-简单POST方法

- 客户端

  ![](https://httpserverandclient.oss-cn-beijing.aliyuncs.com/%E7%AE%80%E5%8D%95POST%E6%96%B9%E6%B3%95_%E5%AE%A2%E6%88%B7%E7%AB%AF.png)

- 服务器端

  ![](https://httpserverandclient.oss-cn-beijing.aliyuncs.com/%E7%AE%80%E5%8D%95post%E6%96%B9%E6%B3%95_%E6%9C%8D%E5%8A%A1%E7%AB%AF.png)

### 3.5-状态码200

- GET 请求成功

  - 客户端

    ![](https://httpserverandclient.oss-cn-beijing.aliyuncs.com/%E7%AE%80%E5%8D%95get%E6%96%B9%E6%B3%95_%E5%AE%A2%E6%88%B7%E7%AB%AF.png)

  - 服务器端

    ![](https://httpserverandclient.oss-cn-beijing.aliyuncs.com/%E7%AE%80%E5%8D%95get%E6%96%B9%E6%B3%95_%E6%9C%8D%E5%8A%A1%E7%AB%AF.png)

### 3.6-状态码301

- 请求的文件永久移动，服务端保存了文件移动记录

  - 客户端

    <img src="https://box.nju.edu.cn/f/1054a26dc0bd41f38519/?dl=1" style="zoom:50%;" />

  - 服务端

    <img src="https://box.nju.edu.cn/f/bc812f14cf994f439b7d/?dl=1" style="zoom:50%;" />

### 3.7-状态码302

- 请求的文件临时移动，服务端保存了文件移动记录：

  - 客户端

    <img src="https://box.nju.edu.cn/f/87f5475e94e046fba03a/?dl=1" style="zoom:50%;" />

  - 服务端

    <img src="https://box.nju.edu.cn/f/bf67c9c5f2cb4be7a32d/?dl=1" style="zoom:50%;" />

### 3.8-状态码304

- 如果请求的文件未被修改，返回状态码304：

  - 客户端

    ![](https://httpserverandclient.oss-cn-beijing.aliyuncs.com/%E7%8A%B6%E6%80%81%E7%A0%81304_%E5%AE%A2%E6%88%B7%E7%AB%AF.png)

  - 服务器端

    ![](https://httpserverandclient.oss-cn-beijing.aliyuncs.com/%E7%8A%B6%E6%80%81%E7%A0%81304_%E6%9C%8D%E5%8A%A1%E7%AB%AF.png)

### 3.9-状态码404

- 请求的文件不存在：

  - 客户端

    ![](https://httpserverandclient.oss-cn-beijing.aliyuncs.com/%E7%8A%B6%E6%80%81%E7%A0%81404_%E5%AE%A2%E6%88%B7%E7%AB%AF.png)

  - 服务器端

    ![](https://httpserverandclient.oss-cn-beijing.aliyuncs.com/%E7%8A%B6%E6%80%81%E7%A0%81404_%E6%9C%8D%E5%8A%A1%E7%AB%AF.png)

### 3.10-状态码405

- 方法不支持：

  - 客户端

    ![](https://httpserverandclient.oss-cn-beijing.aliyuncs.com/%E7%8A%B6%E6%80%81%E7%A0%81405_%E5%AE%A2%E6%88%B7%E7%AB%AF.png)

  - 服务器端

    ![](https://httpserverandclient.oss-cn-beijing.aliyuncs.com/%E7%8A%B6%E6%80%81%E7%A0%81405_%E6%9C%8D%E5%8A%A1%E7%AB%AF.png)

### 3.11-状态码500

- 服务端发生错误：

  - 客户端

    ![](https://httpserverandclient.oss-cn-beijing.aliyuncs.com/%E7%8A%B6%E6%80%81%E7%A0%81500_%E5%AE%A2%E6%88%B7%E7%AB%AF.png)

  - 服务器端

    ![](https://httpserverandclient.oss-cn-beijing.aliyuncs.com/%E7%8A%B6%E6%80%81%E7%A0%81500_%E6%9C%8D%E5%8A%A1%E7%AB%AF.png)

### 3.12-长连接

- HTTP1.1默认长连接，如果请求头部信息包含Connection：close，则关闭连接：

  - 客户端

    ![](https://httpserverandclient.oss-cn-beijing.aliyuncs.com/%E9%95%BF%E8%BF%9E%E6%8E%A5_%E5%AE%A2%E6%88%B7%E7%AB%AF.png)

  - 服务器端

    ![](https://httpserverandclient.oss-cn-beijing.aliyuncs.com/%E9%95%BF%E8%BF%9E%E6%8E%A5_%E6%9C%8D%E5%8A%A1%E7%AB%AF.png)

### 3.13-MIME

- MIME支持三种类型.txt .html以及一种非文本类型.jpeg:

