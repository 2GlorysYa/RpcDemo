package Server.Service.Impl;

import Server.Service.HelloObjectB;
import Server.Service.ServiceB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceBImpl implements ServiceB {

    private static final Logger logger = LoggerFactory.getLogger(ServiceAImpl.class);

    @Override
    public void getInfo(HelloObjectB object) {
        logger.info("接收到个人信息: {} + {} + {}", object.getName(), object.getAge(), object.getGender());
    }
}
