package RPCFW.RPCDemo.client;

import RPCFW.HelloMessage;
import RPCFW.HelloService;
import RPCFW.RPCCommon.ClientProxy;
import RPCFW.RPCDemo.server.HelloServiceImpl;

public class Client {
    public static void main(String[] args) {
        HelloService clientProxy = new ClientProxy("localhost",8080).getProxy(HelloService.class);
        String result = clientProxy.hello(new HelloMessage("hello ", "this is description"));
        System.out.println(result);
    }
}
