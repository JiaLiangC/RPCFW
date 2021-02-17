import RPCFW.ServiceManager.registry.DefaultRegistry;
import RPCFW.Transport.client.NettyClientProxy;
import RPCFW.Transport.server.NettyRpcServer;
import org.junit.Test;
import raft.RaftService;
import raft.RaftServiceImpl;
import raft.common.RaftProperties;

public class RPCTest {


    @Test
    public void testRaftServiceRpc() throws InterruptedException {
        Thread t = new Thread(()->{
            startRPCServer();
        });
        t.start();
        //DefaultRegistry registry = new DefaultRegistry();
       /* RpcClient client = new NettyRpcClient();

        client.connect("localhost",8081);
        RaftService raftService = new ClientProxy(client).getProxy(RaftService.class);*/

        RaftService raftService = new NettyClientProxy("localhost",8081).getProxy(RaftService.class);

        String res = raftService.rpcTest();
        System.out.println(res);

    }

    public void startRPCServer(){
        NettyRpcServer nettyRpcServer = new NettyRpcServer(8081);
        DefaultRegistry registry = new DefaultRegistry();
        registry.register(new RaftServiceImpl());
        nettyRpcServer.start();
    }

    @Test
    public void testLeaderElection() throws InterruptedException {
        MiniRaftCluster cluster = new MiniRaftCluster().newCluster(3,new RaftProperties());
        cluster.start();

    }
}
