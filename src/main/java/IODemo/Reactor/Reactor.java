package IODemo.Reactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

public class Reactor implements Runnable {
    ServerSocketChannel serverSocketChannel;
    Selector selector;

    Reactor() throws IOException {
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(8080));
        serverSocketChannel.configureBlocking(false);
        selector = Selector.open();
        SelectionKey sk = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        sk.attach(new Acceptor());
    }

    @Override
    public void run() {
        try{
            while (!Thread.interrupted()){
                selector.select();
                Set selected = selector.selectedKeys();
                Iterator<SelectionKey> it =  selected.iterator();
                while (it.hasNext()){
                    SelectionKey selectionKey = it.next();
                    dispatch(selectionKey);
                }
                selected.clear();
            }
        }catch (IOException exception){

        }

    }

    public void dispatch(SelectionKey selectionKey){
        Runnable r = (Runnable)selectionKey.attachment();
        if(r!=null){
            r.run();
        }
    }

     class Acceptor implements Runnable{

        @Override
        public void run() {
            try {
                SocketChannel socketChannel = serverSocketChannel.accept();
                if(socketChannel!=null){
                    new Handler(selector,socketChannel);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

}
