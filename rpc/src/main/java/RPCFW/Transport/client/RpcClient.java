package RPCFW.Transport.client;

import RPCFW.Transport.common.RPCRequest;

import java.net.InetSocketAddress;

public interface RpcClient {

    void connect(InetSocketAddress address);
    void connect(String host, int port);
    Object sendRpcRequest(RPCRequest rpcRequest);
}
