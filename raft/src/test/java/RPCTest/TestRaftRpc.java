package RPCTest;

import org.junit.Test;
import raft.common.utils.NetUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

public class TestRaftRpc {


    @Test
    public void BasicRPCTest() {
        //简化所有流程，单纯测试RPC 通信
        List<Peer> group = new ArrayList<>();
        List<TestRaftServiceImpl> servers = new CopyOnWriteArrayList<>();
        ExecutorService service = Executors.newFixedThreadPool(100);


        for (int i = 0; i < 3; i++) {
            group.add(new Peer(String.valueOf(i), NetUtils.createLocalServerAddress()));
        }

        group.forEach(p -> {
            servers.add(new TestRaftServiceImpl(p, group));
        });

        servers.forEach(server -> {
            service.submit(() -> {
                try {
                    server.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        servers.forEach(ser->{
            IntStream.range(0,100).forEach(i->{
                ser.sendRequestVoteToOthers();
            });
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });


    }
}
