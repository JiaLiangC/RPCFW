package IODemo.Reactor;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Handler implements Runnable{
    ExecutorService executorService = Executors.newFixedThreadPool(100);
    final SocketChannel socketChannel;
    final SelectionKey sk;
    static int MAXIN=1024;
    static int MAXOUT=1024;
    static  final int READING=0,SENDING=1;
    private int state = READING;
    ByteBuffer input = ByteBuffer.allocate(MAXIN);
    ByteBuffer output = ByteBuffer.allocate(MAXOUT);

    Handler(Selector sel, SocketChannel ch) throws IOException {
        socketChannel=ch;
        socketChannel.configureBlocking(false);
        sk = socketChannel.register(sel,0);
        sk.attach(this);
        sk.interestOps(SelectionKey.OP_READ);
        sel.wakeup();
    }
    @Override
    public void run() {
        try{
        if(state==READING){
            read();
        }

        if(state==SENDING){
            write();
        }
        }catch (IOException e){

        }
    }

    synchronized void read() throws IOException {
        int len = socketChannel.read(input);
        executorService.submit(new Processor());
        System.out.println(new String(input.array(),0,len));

        state=SENDING;
        sk.interestOps(SelectionKey.OP_WRITE);
    }

    void process(){
        //do something
    }
    synchronized void  processAndHandoff(){
        process();
        state=SENDING;
        sk.interestOps(SelectionKey.OP_WRITE);
    }

    public void write() throws IOException {
        socketChannel.write(output);
        if(!output.hasRemaining()){
            sk.cancel();
        }
    }

    class Processor implements Runnable{

        @Override
        public void run() {

        }
    }


}
