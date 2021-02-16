import org.junit.Test;
import raft.common.utils.NetUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestRaftRpc {


    @Test
    public void test() {
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
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        servers.forEach(ser->{
            ser.sendRequestVoteToOthers();
        });


    }
}