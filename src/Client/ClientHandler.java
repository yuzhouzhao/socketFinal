package Client;

import HTTP.HttpRequest;
import HTTP.HttpResponse;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ClientHandler {

    //解析报文，并处理
    //对mime类型的处理
    //对301、302、304的处理 重定向的处理

    private static String method; //请求报文的方法
    private static String url;  //请求报文的路径
    private static byte[] message_body;  //请求报文的body
    private HttpRequest httpRequest;
    private HttpResponse httpResponse;
    private String newUrl = "";

    private static Map<String, String> mimes = new HashMap<>();


    public ClientHandler(byte[] requestData, byte[] responseData) throws IOException {
        mimes.put("text/plain", ".txt");
        mimes.put("text/html", ".html");
        mimes.put("image/jpeg", ".jpeg");
        //解析请求报文
        StringBuffer request_message = new StringBuffer();
        char temp;

        /**
         * 开始行，\r\n首次出现
         */
        for (int i = 0; i < requestData.length; i++) {
            temp = (char) requestData[i];
//            if(temp == '\r' || temp == '\n'){
//                end_line++;
//            }else {
//                end_line = 0;
//            }

//            if(end_line == 2){
//                break;
//            }
            //这个方法不是会多都进去一个/r？
            if (temp == '\r') {
                if ((char) requestData[i + 1] == '\n')
                    break;
            }
            request_message.append(temp);
        }

        String[] startLine = request_message.toString().split(" ");
        method = startLine[0]; //GET or POST
        url = startLine[1];


        //get message_body
        int flag = 0;
        int j = 0;
        for (; j < requestData.length; j++) {
            temp = (char) requestData[j];
            if (temp == '\r') {
                if ((char) requestData[j + 1] == '\n') {
                    flag++;
                    if (flag == 5)
                        break;
                }

            }
//            if (temp == '\r' || temp == '\n') {
//                flag++;
//            } else {
//                flag = 0;
//            }
//            if (flag == 4) {
//                break;
//            }
        }
        message_body = Arrays.copyOfRange(requestData, j, requestData.length);

        //解析响应报文
        StringBuffer response_message = new StringBuffer();
        flag = 0;
        boolean isBody = false;
        int contentLength = 0;

        ArrayList<Byte> body = new ArrayList<>();
        String request_startLine = "";
        Map<String, String> headers = new HashMap<>();

        /*
        对字节数组进行转换，在\r\n出现两次的情况认为首部结束，剩下的是主体部分
         */
        for (int i = 0; i < responseData.length; i++) {
            if (isBody) {
                body.add(responseData[i]);
            } else {
                temp = (char) responseData[i];
                if (temp == '\r' || temp == '\n') {
                    flag++;
                } else {
                    flag = 0;
                }
                if (flag == 4) {
                    isBody = true;
                }
//                if(temp=='\r'){
//                    if((char)responseData[i+1]=='\n') {
//                        flag++;
//                        if(flag==5)
//                            isBody=true;
//                    }
//
//                }
                response_message.append(temp);
            }
        }
        /*
        对开始行和首部信息进行读取，默认每行的结尾都是\r\n
         */
        String[] text = response_message.toString().split("\r\n");
        request_startLine = text[0];

        for (int i = 1; i < text.length; i++) {
            if (text[i] != "") {
                String[] header = text[i].split(": ");
                headers.put(header[0], header[1]);
            }
        }

        if (headers.containsKey("Content-length")) {
            contentLength = Integer.parseInt(headers.get("Content-length"));
        }

        /*
        将主体的Byte[]变成byte[]
        是否有更方便的做法？
         */
        byte[] res = new byte[body.size()];
        for (int i = 0; i < body.size(); i++) {
            res[i] = body.get(i);
        }
        httpResponse = new HttpResponse(request_startLine, headers, res);
    }

    /**
     * 客户端做出响应
     * 返回给客户端状态码，提示下一步操作
     */
    public int response() {
        //检查状态码
        int statusCode = httpResponse.getStateCode();
        switch (statusCode) {
            case 200:
                return do200();
            case 301:
                return 301;
            case 302:
                return 302;
            case 304:
                return do304();
            case 404:                                           //暂定
//                System.out.println("Not found");
                return 404;
            case 405:
//                System.out.println("方法不支持。");
                return 405;
            default:
//                System.out.println("Internal server error");
                return 500;
        }
    }

    public void show(String fileName) {
        String filePath = "src\\Client\\Resource" + fileName;
        String mime = httpResponse.getHeader("Content-type");
        switch (mime) {
            case ".txt":
                filePath += ".txt";
                break;
            case ".html":
                filePath += ".html";
                break;
            case ".jpeg":
                filePath += ".jpeg";
                break;
        }
        File file = new File(filePath);
        try {
            Desktop.getDesktop().open(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private int do200() {
        if (method.equals("POST")) {
            //请求报文方法是POST
            System.out.println("服务器端已收到。"); //暂定
            return 2001;
        }

        //请求报文是get方法
        //fileName从响应报文的url里来
        String fileName = getFileNameFromUrl(url);
        fileName = "src/Client/Resource/" + fileName;
        File file = new File(fileName);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            if (httpResponse.getHeader("Content-type").equals("image/jpeg")) {
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(httpResponse.getBody());
                fos.flush();
                fos.close();
            } else {
                FileWriter fw = new FileWriter(file);
                fw.write(new String(httpResponse.getBody()));
                fw.close();
            }
            System.out.println("文件已保存在" + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 2002;

    }

    public byte[] do301() {
        //更新URL
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "*");
        String response_url = new String(Arrays.copyOfRange(httpResponse.getBody(), 18, httpResponse.getBody().length));
        newUrl = response_url;
        headers.put("Host", response_url);
        httpRequest = new HttpRequest(buildStartLine(response_url), headers, message_body);
        return http2bytes();
    }

    public byte[] do302() {
        //更新URL
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "*");
        String response_url = new String(Arrays.copyOfRange(httpResponse.getBody(), 21, httpResponse.getBody().length));
        headers.put("Host", response_url);
        httpRequest = new HttpRequest(buildStartLine(response_url), headers, message_body);
        return http2bytes();
    }

    private int do304() {
        System.out.println("已刷新。");
        return 304;
    }

    private String buildStartLine(String url) {
        //   System.out.println("-------new url is "+url);
        //请求报文<method><url><version>
        StringBuffer sb = new StringBuffer();
        sb.append(method);
        sb.append(" ");
        sb.append(url);
        sb.append(" HTTP/1.1 ");
        return sb.toString();
    }

    private byte[] http2bytes() {
        String temp = "";
        temp = temp + httpRequest.getMethod() + " " + httpRequest.getUrl() + " " + httpRequest.getVersion() + "\r\n";
        Map<String, String> headers = httpRequest.getHeaders();
        for (String key : headers.keySet()) {
            temp = temp + key + ": " + headers.get(key) + "\r\n";
        }
        temp = temp + "\r\n" + httpRequest.getBody().toString() + "\r\n";
        return temp.getBytes();
    }

    private String getFileNameFromUrl(String url) {
        String[] t = url.split("/");
        if (t.length == 0) {
            return url;
        } else {
            return t[t.length - 1];
        }
    }

    public String getNewUrl() {
        return newUrl;
    }

    public String getRequestStartLineAndHeaders() {
        return httpRequest.startLineAndHeadersToString();
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
}