package RPCFW.ServiceManager.registry;

import RPCFW.RPCDemo.Netty.EatService;
import RPCFW.RPCDemo.Netty.EatServiceImpl;
import RPCFW.ServiceManager.NetNode;
import RPCFW.ServiceManager.ResourceInfo;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class DefaultRegistry {

    //因遍历所有key 速度很慢，所以单独存储用来判断
    private static ConcurrentHashMap <String, Map> router;
    private static  ConcurrentHashMap<String,Object> serviceMap;
    private static  Set<String> services;

    static {
        router = new ConcurrentHashMap();
        serviceMap = new ConcurrentHashMap<>();
        services = new CopyOnWriteArraySet<>();
    }

    /* 调用的时候，是用的接口，以及对应方法名
    * 但一个接口可能会有多个实现类，注册的时候，请求来了如果只有类名会无法区分
    * 接口实现类 需要版本号，ID来区分
    * service 注册的对象
    *  */
    synchronized public <T> void register(T serviceimpl) {

       String serviceName = serviceimpl.getClass().getCanonicalName();
        if (services.contains(serviceName)) {
            return;
        }
        services.add(serviceName);

        Class[] interfaces = serviceimpl.getClass().getInterfaces();
        //注册实现类的所有接口
        for (Class anInterface : interfaces) {
            serviceMap.put(anInterface.getCanonicalName(), serviceimpl);
        }
    }

    /* 调用的时候，是用的接口，以及对应方法名
     * 但一个接口可能会有多个实现类，注册的时候，请求来了如果只有类名会无法区分
     * 接口实现类 需要版本号，ID来区分
     * service 注册的对象
     *  */
    synchronized public <T> void register(ConcurrentHashMap<String,Object> servicesMap,T serviceimpl) {
        Class[] interfaces = serviceimpl.getClass().getInterfaces();
        //注册实现类的所有接口
        for (Class anInterface : interfaces) {
            servicesMap.put(anInterface.getCanonicalName(), serviceimpl);
        }
    }

    synchronized public <T> void newRegister(InetSocketAddress addr, T serviceimpl) {
           String port =  String.valueOf(addr.getPort());
           ConcurrentHashMap smap = new ConcurrentHashMap();
           register(smap, serviceimpl);
            System.out.println(port+"----newRegister");
           router.put(port,smap);

    }

    synchronized public Object getService(int port, String serviceName) {
        Map smap  = router.get(String.valueOf(port));
        return smap.get(serviceName);
    }

    public Object getService(String serviceName) {

        Object service =  serviceMap.get(serviceName);
        return service;
    }

    public void cancel(String serviceName) {
    }

    public List<NetNode> getAddressList(ResourceInfo resourceInfo) {
        return null;
    }

    public static void main(String[] args) {
        DefaultRegistry defaultRegistry = new DefaultRegistry();
        defaultRegistry.register(new EatServiceImpl());
        Object x = defaultRegistry.getService(EatService.class.getCanonicalName());
        System.out.println(x);
    }
}
