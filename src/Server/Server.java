package Server;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class Server extends Thread {
    private ServerSocket serverSocket;

    public Server(int portNum) throws IOException {
        serverSocket = new ServerSocket(portNum);
    }

    @Override
    public void run() {
        while (true) {
            try {
                System.out.println("等待远程连接，端口号为：" + serverSocket.getLocalPort() + "...");
                Socket server = serverSocket.accept();
                System.out.println("连接成功，远程主机地址：" + server.getRemoteSocketAddress() + "\n");

                InputStream ips = server.getInputStream();
                OutputStream ops = server.getOutputStream();

                // 等待message
                while (true) {
                    Thread.sleep(200);
                    byte[] b = new byte[ips.available()];
                    if (ips.read(b) != 0) {
                        ServerHandler httpServerHandler = new ServerHandler(b);
                        System.out.println("***收到新报文***");
                        httpServerHandler.process(); // 处理message
                        System.out.println(httpServerHandler.getRequestStartLineAndHeaders());
                        ops.write(httpServerHandler.getResponse());
                        ops.flush();
//                        System.out.println("***响应报文已发送***\n" + httpServerHandler.getResponseStartLineAndHeaders());
                        System.out.println(httpServerHandler.getResponseString());
                        if (!httpServerHandler.getConnectionState()) {
                            System.out.println("***长连接关闭***");
                            break;
                        }
                    }
                }
                server.close();
            } catch (SocketTimeoutException s) {
                System.out.println("Socket timed out!");
                break;
            } catch (IOException e) {
                e.printStackTrace();
                break;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        int port = 8080;
        try {
            Thread server = new Server(port);
            server.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
