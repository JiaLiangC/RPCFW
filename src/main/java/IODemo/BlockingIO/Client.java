package IODemo.BlockingIO;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Client {
    public final static int PORT = 9098;
    public final static String IP = "localhost";


    public static void main(String[] args) {
        new Client().connect();

    }

    public void connect() {
        byte[] buffer = new byte[1024];
        Socket socket = null;
        OutputStream outputStream = null;
        InputStream inputStream = null;
        try {
            socket = new Socket(IP, PORT);
            outputStream = socket.getOutputStream();
            byte[] content = "hello Server".getBytes();
            outputStream.write(content);
            outputStream.flush();

            inputStream = socket.getInputStream();
            inputStream.read(buffer);
            String res = new String(buffer,"UTF-8");
            System.out.println(res);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                inputStream = null;
            }

            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                outputStream = null;
            }

            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
