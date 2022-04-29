package client.lb.Impl;

import client.lb.LoadBalancer;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

// 随机负载均衡算法
public class RandomLoadBalancer implements LoadBalancer {

    /**
     * @param addressList 需要负载均衡的服务列表
     * Random底层是CAS，在多线程下效率会降低
     * 因此多线程下使用ThreadLocalRandom来为每一个线程生成一个随机数，保证这个数是线程隔离
     */
    @Override
    public String select(List<String> addressList) {
        int nodeSize = addressList.size();
        return addressList.get(ThreadLocalRandom.current().nextInt(nodeSize));  // 上界
    }

    @Override
    public String select(List<String> addressList, int hashCode) {
        return null;
    }
}
