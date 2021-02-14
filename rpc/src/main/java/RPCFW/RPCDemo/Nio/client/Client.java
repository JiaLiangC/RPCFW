package RPCFW.RPCDemo.Nio.client;

import RPCFW.HelloMessage;
import RPCFW.HelloService;
import RPCFW.Transport.client.ClientProxy;
import RPCFW.Transport.client.RpcClient;
import RPCFW.Transport.client.SocketRpcClient;

public class Client {
    public static void main(String[] args) {

        RpcClient client = new SocketRpcClient("localhost",8080);
        HelloService clientProxy = new ClientProxy(client).getProxy(HelloService.class);
        String result = clientProxy.hello(new HelloMessage("hello ", "this is description"));
        System.out.println(result);
    }
}
