package IODemo.MultiplexingIO;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {

    static ExecutorService executorService;

    static {
        executorService = Executors.newFixedThreadPool(1);
    }

    @Override
    public void run() {
        try {
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(8080));
            serverSocketChannel.configureBlocking(false);
            Selector selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            while (!Thread.interrupted()) {
                if (selector.select(3000) == 0) {
                    System.out.println("no message received");
                    continue;
                }

                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> iterableKeys = keys.iterator();
                while (iterableKeys.hasNext()) {
                    SelectionKey selectionKey = iterableKeys.next();
                    //防止重复处理
                    iterableKeys.remove();

                    if (selectionKey.isAcceptable()) {

                        executorService.submit(() -> {
                            ServerSocketChannel serverChannel = (ServerSocketChannel) selectionKey.channel();
                            try {
                                SocketChannel socketChannel = serverChannel.accept();
                                socketChannel.configureBlocking(false);
                                //accept 连接后注册监听连接的读事件
                                socketChannel.register(selector, SelectionKey.OP_READ);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                        continue;
                    }

                    //executorService.submit(new Handler(selectionKey));
                    new Handler(selectionKey).run();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    static class Handler implements Runnable {
        final SelectionKey selectionKey;

        Handler(SelectionKey key) {
            selectionKey = key;
        }

        @Override
        public void run() {
            try {
                if (selectionKey.isReadable()) {
                    read();
                } else if (selectionKey.isValid() && selectionKey.isWritable()) {
                    send();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void read() throws IOException {
            SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
            ByteBuffer buffer = ByteBuffer.wrap(new byte[1024]);
            int len = socketChannel.read(buffer);
            //len=-1 表示客户端断开连接了
            if (len == -1) {
                socketChannel.close();
                return;
            }
            System.out.println("server received messages:");
            System.out.println(new String(buffer.array(), 0, len));

            selectionKey.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        }

        public void send() throws IOException {
            String message = "server message: hello client";
            ByteBuffer writeBuffer = ByteBuffer.wrap(message.getBytes());
            SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
            if (socketChannel.isOpen()) {
                System.out.println("server send message: "+message);
                socketChannel.write(writeBuffer);
            }

            if(!writeBuffer.hasRemaining()){
                selectionKey.interestOps(SelectionKey.OP_READ);
            }
            writeBuffer.compact();
        }
    }


    public static void main(String[] args) {
        Server server = new Server();
        Thread t= new Thread(server);
        t.start();

    }
}