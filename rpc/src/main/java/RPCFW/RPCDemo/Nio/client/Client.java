package RPCFW.RPCDemo.Nio.client;

import RPCFW.HelloMessage;
import RPCFW.HelloService;
import RPCFW.Transport.client.NettyClientProxy1;
import RPCFW.Transport.client.RpcClient;
import RPCFW.Transport.client.SocketRpcClient;

import java.net.InetSocketAddress;

public class Client {
    public static void main(String[] args) {

        HelloService clientProxy = new NettyClientProxy1(new InetSocketAddress(8080)).getProxy(HelloService.class);
        String result = clientProxy.hello(new HelloMessage("hello ", "this is description"));
        System.out.println(result);
    }
}
