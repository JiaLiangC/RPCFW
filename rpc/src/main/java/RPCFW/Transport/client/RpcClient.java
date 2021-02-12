package RPCFW.Transport.client;

import RPCFW.Transport.common.RPCRequest;

public interface RpcClient {

    Object sendRpcRequest(RPCRequest rpcRequest);
}
