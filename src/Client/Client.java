package Client;

import Client.UI.LoginUI;
import HTTP.HttpRequest;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Client extends Thread{
    private static Socket client=null;
    private static Map<String,String> fileMap;//已知服务器的映射

    public static void main(String[] args) throws InterruptedException {
        Client t = new Client(8080);
        t.start();
//        t.run(null);
        System.out.println("login or not? [Y/N]");
        Scanner scanner=new Scanner(System.in);
        String code=scanner.nextLine();
        if (code.equals("Y")){
            new LoginUI();
        }
        else
            run(null);

    }

    public Client(int port) {
        try
        {
            client = new Socket("127.0.0.1", port);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public static void run(HttpRequest httpRequest) {
        String NewUrl="";
        setFileMap();//初始化fileMap
        try
        {

            InputStream receiveStream = client.getInputStream();//服务器端发回的数据
            OutputStream sendStream = client.getOutputStream();//发送给服务器端的数据

            //这个线程会五秒访问一次服务器，调试的时候为了方便可以注释掉！不然会五秒钟跳动一次烦你
            // new Thread(new Client_heart(client,sendStream)).start();//保持长连接，心跳包！！每间隔五秒钟朝服务器发送请求以保证实现长连接

            System.out.println("Client is ready.");
            while (true){

                RequestHandler requestHandler;
                requestHandler=new RequestHandler(fileMap,httpRequest);
                if(httpRequest==null)
                {

                    System.out.println("");
                    System.out.println("***REQUEST***");
                    System.out.println(requestHandler.getRequest().startLineAndHeadersToString());
                }
                else
                {
//                    httpRequest=null;
                }
                sendStream.write(requestHandler.getHttpRequest());


                boolean chatting=true;
                while (chatting){
                    //Response
                    //get available byte[].length=availble
                    int count = 0;
                    Thread.sleep(300);

                    do {
                        count = receiveStream.available();
                    }while(count==0);
                    byte[] temp=new  byte[count];
                    receiveStream.read(temp);

                    //handle response
                    ClientHandler hch=new ClientHandler(requestHandler.getHttpRequest(),temp);

                    int state=hch.response();

                    Thread.sleep(300);
                    System.out.println("***RESPONSE***");
//                    System.out.println(hch.getResponseStartLineAndHeaders());
                    System.out.println(hch.getResponseString());

                    switch(state){
                        case 301:
                            sendStream.write(hch.do301());
                            System.out.println("***REQUEST***");
                            System.out.println(hch.getRequestStartLineAndHeaders());
                            NewUrl = hch.getNewUrl();
                            fileMap.put(requestHandler.getFileName_Suffix(),NewUrl);
                            //发给服务器端
                            break;
                        case 302:
                            System.out.println("***REQUEST***");
                            System.out.println(hch.getRequestStartLineAndHeaders());
                            sendStream.write(hch.do302());
//                        System.out.println("---新报文已发送---");
                            //发给服务器端
                            break;
                        default:
                            chatting=false;
                            break;
                    }
                    if(!chatting)
                        break;

//                    if(state==301) {
//                        sendStream.write(hch.do301());
//                        System.out.println("***REQUEST***");
//                        System.out.println(hch.getRequestStartLineAndHeaders());
//                        NewUrl = hch.getNewUrl();
//                        fileMap.put(requestHandler.getFileName_Suffix(),NewUrl);
//                        //发给服务器端
//                    }
//                    else if(state==302){
//                        System.out.println("***REQUEST***");
//                        System.out.println(hch.getRequestStartLineAndHeaders());
//                        sendStream.write(hch.do302());
////                        System.out.println("---新报文已发送---");
//                        //发给服务器端
//                    }else {
//                        break;
//                    }

                    sendStream.flush( );
                    Thread.sleep(4000);
                }

                if(httpRequest==null)
                {
                    String code="";
                    System.out.println("exit or not? [Y/N]");
                    Scanner scanner=new Scanner(System.in);
                    code=scanner.nextLine();
                    if(code.equals("Y")){
                        return;
                    }
                }
                else
                {
                    return;
                }


            }
        }


        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void setFileMap(){
        fileMap=new HashMap<>();
        String DefaultTargetUrl="src/Server/Resource/New/";
        fileMap.put("1.jpeg",DefaultTargetUrl+"1.jpeg");
        fileMap.put("1_301.jpeg",DefaultTargetUrl+"1_301.jpeg");
        fileMap.put("1_302.jpeg",DefaultTargetUrl+"1_302.jpeg");
        fileMap.put("2.txt",DefaultTargetUrl+"2.txt");
        fileMap.put("2_301.txt",DefaultTargetUrl+"2_301.txt");
        fileMap.put("2_302.txt",DefaultTargetUrl+"2_302.txt");
        fileMap.put("3.html",DefaultTargetUrl+"3.html");
        fileMap.put("3_301.html",DefaultTargetUrl+"3_301.html");
        fileMap.put("3_302.html",DefaultTargetUrl+"3_302.html");
        fileMap.put("4.txt",DefaultTargetUrl+"4.txt");
    }


}

//下面这段也不用改！
class Client_heart implements Runnable {
    private Socket socket;
    private OutputStream os;
    private Map<String,String> fileMap=new HashMap<>();
    Client_heart(Socket socket, OutputStream oos){
        this.socket = socket;
        this.os = oos;
    }

    @Override
    public void run() {
        fileMap.put("isheart","true");
        try {
            System.out.println("长连接心跳线程已启动...");
            while (true){
                Thread.sleep(5000);
                RequestHandler requestHandler=new RequestHandler(fileMap,null);
                System.out.println("");
                System.out.println("***可访问服务器，长连接状态保持***");
                System.out.println(requestHandler.getRequest().startLineAndHeadersToString());
                os.write(requestHandler.getHttpRequest());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
