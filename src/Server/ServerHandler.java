package Server;


import HTTP.HttpRequest;
import HTTP.HttpResponse;

import java.io.*;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ServerHandler {
    private HttpRequest httpRequest;//request 报文
    private HttpResponse httpResponse;//response 报文

    private Map<Integer, String> codeAndMessage = new HashMap<>();// 状态码和对应message
    private Map<String, String> mimeType = new HashMap<>();// MIME类型
    private Map<String, String> reversedMime = new HashMap<>();// 反向查找MIME类型

    private Map<String, String> fileStatus = new HashMap<>();// {文件名，状态(valid, deleted, temp)}
    private Map<String, String> filePath = new HashMap<>();// {文件名，路径}
    private Map<String, String> tempFile = new HashMap<>();// {文件名，临时文件名}

    private static Map<String, String> Account = new HashMap<>();//储存注册的账户
    private static Map<String, String> CookieToAccount = new HashMap<>();//储存已经登录的用户的cookie和对应的用户
    private static Map<String, String> AccountToCookie = new HashMap<>();//储存已登录的用户和对应的cookie
    private static int LoggedUser = 0;//储存已经登录的用户数目


    /**
     * ServerHandler 的构造方法
     * 从请求报文 data 中获取 startLine、head 和 body
     * 考虑到主体部分有非文字格式，采用【字节流】而不是【字符流】
     *
     * @param data 从socket的【字节流】得到请求报文的【字节】信息
     * @throws IOException
     */
    public ServerHandler(byte[] data) throws IOException {
        // 初始化文件信息和http信息
        initFileStatus();
        initMessage();

        String startLine;//请求报文的首行
        Map<String, String> headers = new HashMap<>();//存储请求报文的首部和对应的值
        byte[] content;//请求报文的主体部分（如果有的话）
        StringBuilder stringBuilder;

        // 获得请求报文的首行
        stringBuilder = new StringBuilder();
        int startLineTail = 0;
        for (int i = 0; i < data.length; i++) {
            if (data[i] == '\r') {
                if (data[i + 1] == '\n') {
                    startLineTail = i;
                    break;
                }
            }
            stringBuilder.append((char) data[i]);
        }
        startLine = stringBuilder.toString();

        // 获得请求报文的头部，头部和主体部分直接有一个空白行，即两个 '\r\n'
        stringBuilder = new StringBuilder();
        int headerTail = 0;
        for (int i = startLineTail + 2; i < data.length; i++) {
            if (data[i] == '\r') {
                if (data[i + 1] == '\n' && data[i + 2] == '\r' && data[i + 3] == '\n') {
                    headerTail = i;
                    break;
                }
            }
            stringBuilder.append((char) data[i]);
        }
        String header = stringBuilder.toString();

        // 将头部的名称和值加入到 Map<String, String> headers
        String[] headerSplit = header.split("\r\n");
        for (String str : headerSplit) {
            String[] headerTemp = str.split(": ");
            headers.put(headerTemp[0], headerTemp[1]);
        }

        //获得请求报文的内容
        content = new byte[data.length - headerTail - 4];
        for (int i = headerTail + 4; i < data.length; i++) {
            content[i - headerTail - 4] = data[i];
        }

        //初始化HTTP的request报文
        httpRequest = new HttpRequest(startLine, headers, content);
    }


    /**
     * 初始化一些有用的Message
     * codeAndMessage: 【状态码】【描述文本】
     * mimeType: MIME格式 Content-Type
     * reversedMime: 反向查找mimeType
     */
    public void initMessage() {
        codeAndMessage.put(200, "OK"); // 客户端请求成功
        codeAndMessage.put(301, "Moved Permanently"); // 永久重定向 是指请求的资源已被永久的移动到新的地址
        codeAndMessage.put(302, "Found"); // 临时重定向，与301类似。但是资源只是临时被移动。客户端应该继续使用原有的地址
        codeAndMessage.put(304, "Not Modified"); // 未修改，所请求的资源未修改，服务器返回此状态码时，不会返回任何资源。
        codeAndMessage.put(404, "Not Found"); // 请求的资源不存在
        codeAndMessage.put(405, "Method Not Allowed"); // 方法不允许
        codeAndMessage.put(500, "Internal Server Error"); // 服务器发生不可预期的错误，导致无法完成客户端的请求。

        mimeType.put("text/plain", ".txt");
        mimeType.put("text/html", ".html");
        mimeType.put("image/jpeg", ".jpeg");

        reversedMime.put("txt", "text/plain");
        reversedMime.put("html", "text/html");
        reversedMime.put("jpeg", "image/jpeg");
    }


    void initFileStatus() { //文件初始化函数
        //保存在原路径的
        fileStatus.put("1.jpeg", "valid");
        fileStatus.put("1.png", "valid");
        fileStatus.put("2.txt", "valid");
        fileStatus.put("3.html", "valid");
        //301,文件在newPath文件夹里
        fileStatus.put("1_301.jpeg", "valid");
        fileStatus.put("2_301.txt", "valid");
        fileStatus.put("3_301.html", "valid");
        //302
        fileStatus.put("1_302.jpeg", "temp");
        fileStatus.put("2_302.txt", "temp");
        fileStatus.put("3_302.html", "temp");
        fileStatus.put("1t.jpeg", "valid");
        fileStatus.put("2t.txt", "valid");
        fileStatus.put("3t.html", "valid");

        fileStatus.put("4.txt", "deleted");

        //200
        filePath.put("1.jpeg", "src/Server/Resource/New/1.jpeg");
        filePath.put("2.txt", "src/Server/Resource/New/2.txt");
        filePath.put("3.html", "src/Server/Resource/New/3.html");
        //301
        filePath.put("1_301.jpeg", "src/Server/Resource/NewPath/1_301.jpeg");
        filePath.put("2_301.txt", "src/Server/Resource/NewPath/2_301.txt");
        filePath.put("3_301.html", "src/Server/Resource/NewPath/3_301.html");
        //302
        filePath.put("1t.jpeg", "src/Server/Resource/Temp/1t.jpeg");
        filePath.put("2t.txt", "src/Server/Resource/Temp/2t.txt");
        filePath.put("3t.html", "src/Server/Resource/Temp/3t.html");

        filePath.put("1.png", "src/Server/Resource/1.png");

        // 文件名，临时文件名
        tempFile.put("1_302.jpeg", "1t.jpeg");
        tempFile.put("2_302.txt", "2t.txt");
        tempFile.put("3_302.html", "3t.html");
    }

    /**
     * 处理指令，获得响应报文
     * 1）GET 方法
     * 2）POST 方法
     * 3）其他的方法不支持，返回状态码405
     *
     * @return HTTP的response报文（成员变量）
     */
    public HttpResponse process() {
        String method = httpRequest.getMethod();
        switch (method) {
            case "GET":
                doGet();
                break;
            case "POST":
                doPost();
                break;
            default:
                do405();
        }
        return this.httpResponse;
    }


    /**
     * 处理GET方法
     * 获得GET方法要获取的文件的路径、名称、文件类型
     */
    private void doGet() {
        String url = httpRequest.getUrl();
        String ifModified = httpRequest.getHeader("If-Modified-Since");
        if (ifModified != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            File f = new File(url);
            try {
                //如果headers有“If-Modified-Since”则需判断文件是否在这个时间以后修改了，如果没修改则返回304状态码
                if (new Date(f.lastModified()).after(sdf.parse(ifModified))) {
                    String type = url.split("\\.")[1];
                    do200(readFile(url), type);
                } else {
                    do304();
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            String fileName = getFileNameFromUrl(url);
            String type = fileName.split("\\.")[1];
            String status = fileStatus.get(fileName);
            status = (status == null) ? "" : status;
            switch (status) {
                case "valid":
                    if (url.equals(filePath.get(fileName))) {
                        do200(readFile(url), type);
                    } else {
                        do301(filePath.get(fileName));
                    }
                    break;
                case "temp":
                    String tempFileName = tempFile.get(fileName);
                    String tempUrl = "src/Server/Resource/Temp/" + tempFileName;
                    do302(tempUrl);
                    break;
                case "deleted":
                default:
                    do404();
            }
        }
    }


    /**
     * 处理POST方法
     */
    private void doPost() {
        String url = httpRequest.getUrl();
        String contentType = httpRequest.getHeader("Content-type");
        String contentLength = httpRequest.getHeader("Content-length");

        //判断cookie.如果cookie有效且为登录请求则直接登录
        String cookie = httpRequest.getHeader("Cookie");
        if (cookie != null) {
            if (url.equals("/login")) {
                String account = CookieToAccount.get(cookie);
                if (account != null) {
                    do200(account + ",you are logged in.");
                }
            }
        }

        if (contentType == null || contentLength == null) {
            do500();
        } else {

            //在这添加登录和注册接口？
            if (url.equals("/register")) {
                register(httpRequest.getBody());
            } else if (url.equals("/login")) {
                login(httpRequest.getBody());
            } else {

                int length = Integer.parseInt(contentLength);
                if (length == httpRequest.getBody().length) {
                    String fileName = getNewFileName(contentType);
                    File f = new File(url + fileName);
                    if (!f.exists()) {
                        try {
                            f.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                            do500();
                        }
                    }
                    try {
                        FileOutputStream fop = new FileOutputStream(f);
                        fop.write(httpRequest.getBody());
                        fop.flush();
                        fop.close();
                        fileStatus.put(fileName, "valid");
                        filePath.put(fileName, url + fileName);
                    } catch (IOException e) {
                        e.printStackTrace();
                        do500();
                    }
                    do200("Received!");
                } else {
                    do500();
                }

            }
        }
    }


    /**
     * 200 OK：客户端请求成功
     * 设置this.httpResponse(HTTP的response报文)
     *
     * @param body body为资源内容
     * @param type type为MIME类型
     */
    private void do200(byte[] body, String type) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-type", reversedMime.get(type));
        headers.put("Content-length", String.valueOf(body.length));
        this.httpResponse = new HttpResponse(buildStartLine(200), headers, body);
    }

    private void do200(String prompt) {
        Map<String, String> headers = new HashMap<>();
        byte[] body = prompt.getBytes();
        headers.put("Content-type", "text/plain");
        headers.put("Content-length", String.valueOf(body.length));
        this.httpResponse = new HttpResponse(buildStartLine(200), headers, body);
    }


    /**
     * 301 Moved Permanently：永久重定向，是指请求的资源已被永久的移动到新的地址
     * 浏览器还会自动定向到新的地址
     * 今后任何新的请求都应该使用新的地址来代替。
     * 设置this.httpResponse(HTTP的response报文)
     *
     * @param newPath 新地址
     */
    private void do301(String newPath) {
        Map<String, String> headers = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        headers.put("Date", sdf.format(new Date()));
        headers.put("Location", newPath);
        String type = newPath.split("\\.")[1];
        headers.put("Content-type", type);
        String s = "资源新地址：" + newPath;
        byte[] body = s.getBytes();
        headers.put("Content-length", String.valueOf(body.length));
        this.httpResponse = new HttpResponse(buildStartLine(301), headers, body);
    }


    /**
     * 302 Found：临时重定向，与301类似。
     * 但是资源只是临时被移动。客户端应该继续使用原有的地址
     * 设置this.httpResponse(HTTP的response报文)
     *
     * @param tempPath 临时地址
     */
    private void do302(String tempPath) {
        Map<String, String> headers = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        headers.put("Date", sdf.format(new Date()));
        headers.put("Location", tempPath);
        headers.put("Content-type", "text/plain");
        String s = "资源临时地址：" + tempPath;
        byte[] body = s.getBytes();
        headers.put("Content-length", String.valueOf(body.length));

        this.httpResponse = new HttpResponse(buildStartLine(302), headers, body);
    }


    /**
     * 304 Not Modified：未修改，所请求的资源未修改，服务器返回此状态码时，不会返回任何资源。
     * 客户端通常会缓存所访问过的资源。
     * 通过提供一个头信息指出客户端希望只返回在指定日期之后修改的资源。
     * 设置this.httpResponse(HTTP的response报文)
     */
    private void do304() {
        Map<String, String> headers = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        headers.put("Date", sdf.format(new Date()));
        this.httpResponse = new HttpResponse(buildStartLine(304), headers, null);
    }


    /**
     * 404 Not Found：请求的资源不存在
     * 例如，输入了错误的URL
     *
     * 设置this.httpResponse(HTTP的response报文)
     */
    private void do404() {
        Map<String, String> headers = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        headers.put("Date", sdf.format(new Date()));
        headers.put("Content-type", "text/plain");
        String s = "文件不存在！";
        byte[] body = s.getBytes();
        headers.put("Content-length", String.valueOf(body.length));
        this.httpResponse = new HttpResponse(buildStartLine(404), headers, body);
    }


    /**
     * 405 Method Not Allowed：方法不允许，对于请求所标识的资源，不允许使用请求行中所指定的方法。
     * 如：
     * 1）get/post/put/delete/请求用混淆了
     * 2）为所请求的资源设置了不正确的 MIME 类型。
     *
     * 设置this.httpResponse(HTTP的response报文)
     */
    private void do405() {
        Map<String, String> headers = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        headers.put("Date", sdf.format(new Date()));
        headers.put("Allow", "GET, POST");
        this.httpResponse = new HttpResponse(buildStartLine(405), headers, null);
    }


    /**
     * 500 Internal Server Error：服务器发生不可预期的错误，导致无法完成客户端的请求。
     * 设置this.httpResponse(HTTP的response报文)
     */
    private void do500() {
        Map<String, String> headers = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        headers.put("Date", sdf.format(new Date()));
        this.httpResponse = new HttpResponse(buildStartLine(500), headers, null);
    }


    private byte[] readFile(String url) {
        File file = new File(url);
        try {
            InputStream in = new FileInputStream(file);
            int count = 0;
            while (count == 0) {
                count = in.available();
            }
            byte[] b = new byte[count];
            in.read(b);
            return b;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    private String getFileNameFromUrl(String url) {
        String[] t = url.split("/");
        if (t.length == 0) {
            return url;
        } else {
            return t[t.length - 1];
        }
    }


    private String getNewFileName(String type) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String fileName = sdf.format(new Date()) + mimeType.get(type);
        int i = 1;
        String status = fileStatus.get(fileName);
        while (status != null && !"deleted".equals(status)) {
            fileName = sdf.format(new Date()) + "(" + i + ")" + type;
            i++;
            status = fileStatus.get(fileName);
        }
        return fileName;
    }


    private String buildStartLine(int code) {
        return "HTTP/1.1 " + code + " " + codeAndMessage.get(code);
    }


    public boolean getConnectionState() {
        return httpRequest.getConnectionState();
    }


    public String getRequestStartLineAndHeaders() {
        return httpRequest.startLineAndHeadersToString();
    }


    public byte[] getResponse() {
        return httpResponse.toByteArray();
    }


    public String getResponseStartLineAndHeaders() {
        return httpResponse.startLineAndHeadersToString();
    }


    public String getResponseString() {
        String body = "";
        if(httpResponse.getBody() != null) {
            body = new String(httpResponse.getBody());
        }
        return getResponseStartLineAndHeaders() + body;
    }


    //账号密码格式 ：username=xxxxxx&password=xxxxxx
    private void register(byte[] body) {//处理注册函数
        String[] tmp = parseUsernameAndPassword(body);
        if (tmp == null) {
            do500();
            return;
        }
        if (Account.get(tmp[0]) != null) {//账号已经被注册过了
            do200("Register fail,this account was registered.");
        } else {
            Account.put(tmp[0], tmp[1]);
            do200("Register succeed,you have a new account:\n" + "username: " + tmp[0] + "\npassword: " + tmp[1]);
        }
    }

    private void login(byte[] body) {//处理登录的函数
        String[] tmp = parseUsernameAndPassword(body);
        if (tmp == null) {
            do500();
            return;
        }
        String password = Account.get(tmp[0]);
        if (password == null) {//账号不存在的情况
            do200("Login fail.This account don't exist.");
        } else if (!password.equals(tmp[1])) {//密码不对的情况
            do200("Login fail.Your password is wrong.");
        } else {//登录成功，并发送cookie
            String Cookie = AccountToCookie.get(tmp[0]);
            if (Cookie == null) {//cookie为null则内存中不含有该账户的cookie，需要新建一个
                LoggedUser += 1;
                Cookie = "User" + LoggedUser;
                CookieToAccount.put(Cookie, tmp[0]);
                AccountToCookie.put(tmp[0], Cookie);
            }
            do200("Login succeed,welcome " + tmp[0] + " !");
        }
    }

    private String[] parseUsernameAndPassword(byte[] body) {//从POST方法的请求内容中解析出账户名和密码
        String s = new String(body);
        s = Decoder(s);
        if (s == null) {
            return null;
        }
        String[] splits = s.split("&");
        String[] re = new String[2];
        if (splits.length < 2 || splits.length > 3) {//register请求格式最后还多一个&ConfirmPassword
            return null;
        } else {
            String[] s1 = splits[0].split("=");
            String[] s2 = splits[1].split("=");
            if (s1[0].toLowerCase().equals("username") && s2[0].toLowerCase().equals("password")) {
                re[0] = s1[1];
                re[1] = s2[1];
                return re;
            } else if (s2[0].toLowerCase().equals("username") && s1[0].toLowerCase().equals("password")) {
                re[0] = s2[1];
                re[1] = s1[1];
                return re;
            } else {
                return null;
            }
        }
    }

    private String Decoder(String s) {//解码器，解析utf-8格式的内容
        try {
            return URLDecoder.decode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

}
