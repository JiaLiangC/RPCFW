package RPCFW.RPCDemo.Netty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EatServiceImpl implements EatService {
    private final Logger logger = LoggerFactory.getLogger(EatServiceImpl.class);

    @Override
    public String eat(Menu menu) {
        logger.info("get menu: food name{}, desc{}",menu.foodName,menu.foodDesc);
        return "eat finished,thamks";
    }
}
