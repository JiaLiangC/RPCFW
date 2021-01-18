package IODemo.BlockingIO;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {
    static ExecutorService pool;
    static {
          pool = Executors.newFixedThreadPool(10);
    }

    public final static int PORT= 9098;

    public void run() {
        try {
            ServerSocket serverSocket =  new ServerSocket(PORT);
            while (!Thread.interrupted()){
               Socket socket = serverSocket.accept();
                pool.submit(new Handler(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    class Handler implements  Runnable{
        private final  Socket socket;
        Handler(Socket s){
            socket = s;
        }

        public void run() {
            byte [] inputBuffer = new byte[1024];
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                 inputStream =  socket.getInputStream();
                //read: This method blocks until input data is available, end of file is detected, or an exception is thrown
                inputStream.read(inputBuffer);
                outputStream =  socket.getOutputStream();
                outputStream.write(process(inputBuffer));

            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if(inputStream != null){
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    inputStream = null;
                }

                if (outputStream!=null){
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    outputStream=null;
                }

                if(socket!=null){
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public byte[] process(byte[] inputBuffer){
            try {
                String res = new String(inputBuffer,"UTF-8");
                System.out.println("received:  "+res);
                String resp = res+"sssdddddd";
                byte[] result =  resp.getBytes();
                return result;
            } catch (Exception e) {
                e.printStackTrace();
                return new byte[0];
            }
        }
    }

    public static void main(String[] args) {
        Thread t = new Thread(new Server());
        t.start();
    }
}
