package Server.Service.Impl;


import Server.Service.HelloObjectA;
import Server.Service.ServiceA;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
@Setter
public class ServiceAImpl implements ServiceA {

    private static final Logger logger = LoggerFactory.getLogger(ServiceAImpl.class);

    @Override
    public String hello(HelloObjectA object) {
        logger.info("接收到: {}", object.getMessage());
        return "这时调用的返回值：id=" + object.getId();
    }
}
