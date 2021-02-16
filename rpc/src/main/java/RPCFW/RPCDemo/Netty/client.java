package RPCFW.RPCDemo.Netty;

import RPCFW.Transport.client.NettyClientProxy1;
import RPCFW.Transport.client.NettyRpcClient;
import RPCFW.Transport.client.RpcClient;

import java.net.InetSocketAddress;

public class client {

    public static void main(String[] args) {
        EatService eatService = new NettyClientProxy1(new InetSocketAddress("localhost",8080)).getProxy(EatService.class);
        String res= eatService.eat(new Menu("apple","juice sweet food"));
        System.out.println(res);
    }
}
