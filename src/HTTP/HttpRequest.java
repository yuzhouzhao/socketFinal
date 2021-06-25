package HTTP;

import java.util.Map;

//request报文实例：
//
//        POST /http://example.org/absolute/path/resource.txt HTTP/1.1
//        HOST: www.XXX.com
//        User-Agent: Mozilla/5.0(Windows NT 6.1;rv:15.0) Firefox/15.0
//
//        Username=admin&password=admin  --》这里是body 例子是注册登录

public class HttpRequest extends HttpObject {

    private String method;
    private String url;
    private String version;

    public HttpRequest(String startLine, Map<String, String> headers, byte[] body){
        this.startLine = startLine;
        String[] startLineStrs = startLine.split(" ");
        method = startLineStrs[0];
        url = startLineStrs[1];
        version = startLineStrs[2];
        this.headers = headers;
        this.body = body;
        this.headerCount = headers.size();
    }

    public String getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    public String getVersion() {
        return version;
    }

    public Map<String,String> getHeaders(){
        return headers;
    }

    public boolean getConnectionState(){
        String state = headers.get("Connection");
        if(state!=null && state.equals("close")){
            return false;
        }else {
            return true;
        }
    }
}
