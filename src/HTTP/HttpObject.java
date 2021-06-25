package HTTP;

import java.util.Map;

//封装http报文格式 报文格式在图片里
public class HttpObject {

    protected String startLine;

    protected int headerCount;

    public String getStartLine() {
        return startLine;
    }

    public void setStartLine(String startLine) {
        this.startLine = startLine;
    }

    public int getHeaderCount() {
        return headerCount;
    }

    public void setHeaderCount(int headerCount) {
        this.headerCount = headerCount;
    }

    public void addHeaders(String name, String value){
        this.headers.put(name, value);
        this.headerCount++;
    }

    public String getHeader(String name){
        if(headers.containsKey(name)){
            return this.headers.get(name);
        }else {
            return null;
        }
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    protected Map<String, String> headers;

    protected byte[] body;

    public String startLineAndHeadersToString() {
        StringBuilder sb = new StringBuilder();
        sb.append(startLine + "\r\n");
        for (String name : headers.keySet()) {
            sb.append(name + ": " + headers.get(name) + "\r\n");
        }
        sb.append("\r\n");
        return sb.toString();
    }

    public byte[] toByteArray(){
        byte[] bt_1 = this.startLineAndHeadersToString().getBytes();
        byte[] bt_2 = this.body;
        if(bt_2 == null){
            return bt_1;
        }else {
            return byteMerger(bt_1, bt_2);
        }
    }

    private static byte[] byteMerger(byte[] bt_1, byte[] bt_2){
        byte[] bt_3 = new byte[bt_1.length+bt_2.length];
        System.arraycopy(bt_1, 0, bt_3, 0, bt_1.length);
        System.arraycopy(bt_2, 0, bt_3, bt_1.length, bt_2.length);
        return bt_3;
    }
}
