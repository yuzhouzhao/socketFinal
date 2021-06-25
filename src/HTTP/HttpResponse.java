package HTTP;

import java.util.Map;

//response报文格式实例：
//        HTTP/1.1 200 OK
//        Content-Encoding: gzip
//        Content-Type: text/html;charset=utf-8
//
//        <!DOCTYPE html>
//        <html lang="en">
//        <head>
//        <meta charset="UTF-8" />
//        <title>Document</title>
//        </head>
//        <body>
//        <p>this is http response</p>
//        </body>
//        </html>

public class HttpResponse extends HttpObject{

    private String version;
    private int stateCode;
    private String reason;


    public HttpResponse(String startLine, Map<String, String> headers, byte[] body){
        this.startLine = startLine;
        String[] startLineStrs = startLine.split(" ");
        version = startLineStrs[0];
        stateCode = Integer.parseInt(startLineStrs[1]);
        reason = startLineStrs[2];
        this.headers = headers;
        this.body = body;
        this.headerCount = headers.size();
    }

    public String getVersion() {
        return version;
    }

    public int getStateCode() {
        return stateCode;
    }

    public Map<String,String> getHeaders(){
        return headers;
    }

}
