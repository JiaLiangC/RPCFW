package IODemo.MultiplexingIO;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class Client implements Runnable{
    private SocketChannel socketChannel;
    private  Selector selector;
    Client(){
        try {
            socketChannel = SocketChannel.open();
            selector  = Selector.open();
            socketChannel.configureBlocking(false);
            socketChannel.register(selector, SelectionKey.OP_CONNECT);
            socketChannel.connect(new InetSocketAddress("localhost",8080));
        } catch (IOException e) {
            e.printStackTrace();
            if (null==socketChannel){
                try {
                    socketChannel.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }


    @Override
    public void run() {
        while (!Thread.interrupted()){
            try {
                selector.select();

                Set<SelectionKey> selectionKeysSet = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeysSet.iterator();

                while (iterator.hasNext()){
                    SelectionKey selectionKey = iterator.next();
                    iterator.remove();

                    if(selectionKey.isConnectable()){
                        connect(selectionKey);
                    }

                    if(selectionKey.isReadable()){
                        read(selectionKey);
                    }

                    if( selectionKey.isWritable()){
                        write(selectionKey);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void connect(SelectionKey selectionKey) throws IOException {
        SocketChannel serverChannel = (SocketChannel) selectionKey.channel();
        serverChannel.finishConnect();
        serverChannel.register(selector,SelectionKey.OP_WRITE);
    }

    public  void read(SelectionKey selectionKey) throws IOException {
        SocketChannel serverChannel = (SocketChannel) selectionKey.channel();
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[1024]);
        int len =  serverChannel.read(byteBuffer);
        if(len==-1){
            serverChannel.close();
            return;
        }
        System.out.println("received message from Server: ");
        System.out.println(new String(byteBuffer.array(),0,len));
    }

    public  void write(SelectionKey selectionKey) throws IOException {
        String message = "hello server";
        SocketChannel serverChannel = (SocketChannel) selectionKey.channel();
        ByteBuffer byteBuffer = ByteBuffer.wrap(message.getBytes());
        System.out.println("write message to server");
        System.out.println(message);
        serverChannel.write(byteBuffer);
        serverChannel.register(selector,SelectionKey.OP_READ);
    }

    public static void main(String[] args) {
        new Client().run();
    }
}
