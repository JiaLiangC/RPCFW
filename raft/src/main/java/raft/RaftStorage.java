package raft;

import java.io.Closeable;
import java.io.IOException;


/**
 * raft 的存储，包括日志和快照在内的内存或持久化存储
 *
 * @author xiaoxiao
 * @date 2021/02/11
 */
public class RaftStorage implements Closeable {

    @Override
    public void close() throws IOException {

    }
}
