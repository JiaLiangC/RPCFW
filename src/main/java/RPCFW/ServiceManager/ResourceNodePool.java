package RPCFW.ServiceManager;

import sun.nio.ch.Net;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ResourceNodePool {
    private static final Map<Integer, List<NetNode>> pool = new ConcurrentHashMap<Integer, List<NetNode>>();


    public static void register(ResourceInfo resourceInfo, NetNode addr){


    }

}
