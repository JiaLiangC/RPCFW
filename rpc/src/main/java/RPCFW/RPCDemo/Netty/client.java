package RPCFW.RPCDemo.Netty;

import RPCFW.Transport.ClientProxy;
import RPCFW.Transport.client.NettyRpcClient;
import RPCFW.Transport.client.RpcClient;

public class client {

    public static void main(String[] args) {
        RpcClient client = new NettyRpcClient("localhost",8080);
        EatService eatService = new ClientProxy(client).getProxy(EatService.class);
        String res= eatService.eat(new Menu("apple","juice sweet food"));
        System.out.println(res);
    }
}
