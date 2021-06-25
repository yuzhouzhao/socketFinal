package Client;

import HTTP.HttpRequest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class RequestHandler {
    private HttpRequest httpRequest;
    private String FileName_Suffix="";
    void handlePost(){
        String startLine = "POST src/Server/Resource/New/ HTTP/1.1";
        Map<String, String> headers = new HashMap<>();
        headers.put("Connection", "keep-alive");
        System.out.println("");
        System.out.println("Input file name with suffix:");
        try {
            Scanner scanner=new Scanner(System.in);
            FileName_Suffix = "src\\Client\\Resource\\";
            FileName_Suffix += scanner.nextLine();
            File file = new File(FileName_Suffix);
            InputStream is = new FileInputStream(file);
            //byte[] body = is.readAllBytes();

            int count = 0;
            do {
                count = is.available();
            }while(count==0);
            byte[] body = new byte[count];
            is.read(body);
            String[] temp=FileName_Suffix.split("\\.");
            switch(temp[1]){
                case "jpeg":
                    headers.put("Content-type","image/jpeg");
                    break;
                case "html":
                    headers.put("Content-type","text/html");
                    break;
                case "txt":
                    headers.put("Content-type","text/plain");
                    break;
            }
            headers.put("Content-length", String.valueOf(body.length));
            httpRequest = new HttpRequest(startLine, headers, body);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    void handleGet(Map<String,String> fileMap){
        Map<String, String> headers = new HashMap<>();
        headers.put("Connection", "keep-alive");
        System.out.println("");
        System.out.println("Input file name with suffix:");
        Scanner scanner=new Scanner(System.in);
        FileName_Suffix=scanner.nextLine();
        String Url=fileMap.get(FileName_Suffix);
        if (Url == null) {
            Url = "src/Server/Resource/New/" + FileName_Suffix;
        }
        String startLine = "GET "+Url+" HTTP/1.1";
        File file =new File("src\\Client\\Resource\\"+FileName_Suffix);
        if(file.exists()){
            Calendar cal = Calendar.getInstance();
            long time = file.lastModified();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            cal.setTimeInMillis(time);
            String IMS=formatter.format(cal.getTime());
            headers.put("If-Modified-Since",IMS);
        }

        String[] temp=FileName_Suffix.split("\\.");
        switch(temp[1]){
            case "jpeg":
                headers.put("Accept","image/jpeg");
                break;
            case "html":
                headers.put("Accept","text/html");
                break;
            case "txt":
                headers.put("Accept","text/plain");
                break;
        }

        httpRequest = new HttpRequest(startLine, headers, null);
    }
    RequestHandler(Map<String,String> fileMap,HttpRequest httpReq) {

        //心跳包实现长连接,下面这个代码块不要改噢！
        if(fileMap.get("isheart")!=null&&fileMap.get("isheart").equals("true")){
            String startLine = "POST src/Server/Resource/Heart/ HTTP/1.1";
            Map<String, String> headers = new HashMap<>();
            headers.put("Connection", "keep-alive");
            FileName_Suffix = "src\\Client\\Resource\\heart.txt";
            headers.put("Accept","text/plain");
            httpRequest = new HttpRequest(startLine, headers, null);
            return;
        }

        if (httpReq!=null)
        {
            httpRequest=httpReq;
            return;
        }
        Scanner scanner = new Scanner(System.in);
        System.out.println("");
        String command="";
        System.out.println("Method:");
        command=scanner.nextLine();

        switch(command)
        {
            case "POST":
                handlePost();
                break;
            case "GET":
                handleGet(fileMap);
                break;
            default:
                String startLine=command+" "+"/"+" HTTP/1.1";
                Map<String, String> headers = new HashMap<>();
                headers.put("Connection", "keep-alive");
                httpRequest=new HttpRequest(startLine,headers,null);
                break;
        }
    }

    public byte[] getHttpRequest(){
        return this.httpRequest.toByteArray();
    }

    public String getFileName_Suffix(){return this.FileName_Suffix;}

    public HttpRequest getRequest(){ return this.httpRequest;}
}
