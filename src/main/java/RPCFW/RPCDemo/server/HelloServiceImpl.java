package RPCFW.RPCDemo.server;

import RPCFW.HelloMessage;
import RPCFW.HelloService;

public class HelloServiceImpl implements HelloService {

    @Override
    public String hello(HelloMessage helloMessage) {
        System.out.println("Rpc server received request");
        System.out.println(helloMessage.getMsg());
        System.out.println(helloMessage.getDesc());
        return "hello client";
    }
}
