package Client.lb.Impl;

import Client.lb.LoadBalancer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinLoadBalancer implements LoadBalancer {

    private static final Logger logger = LoggerFactory.getLogger(RoundRobinLoadBalancer.class);

    // 轮询计数器, 使用原子类保证并发安全
    private AtomicInteger atomicInteger = new AtomicInteger(0);

    /**
     * @param addressList 需要负载均衡的服务列表
     * 轮询
     */
    @Override
    public String select(List<String> addressList) {
        // 如果计数器超过了列表大小，就应该取模保证从头开始轮询
        if (atomicInteger.get() >= addressList.size()) {
            atomicInteger.set(atomicInteger.get() % addressList.size());
        }
        logger.info("当前轮询到第{}个服务器", atomicInteger.get() + 1); // 默认从0开始数
        return addressList.get(atomicInteger.getAndAdd(1));
    }

    @Override
    public String select(List<String> addressList, int hashCode) {
        return null;
    }
}
