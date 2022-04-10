package Client.lb.Impl;

import Client.lb.LoadBalancer;

import java.util.List;

public class RoundRobinLoadBalancer implements LoadBalancer {

    // 轮询计数器
    private int index = 0;

    /**
     * @param addressList 需要负载均衡的服务列表
     * 轮询
     */
    @Override
    public String select(List<String> addressList) {
        // 如果计数器超过了列表大小，就应该取模保证从头开始轮询
        if (index >= addressList.size()) {
            index = index % addressList.size();
        }
        return addressList.get(index++);
    }

    @Override
    public String select(List<String> addressList, int hashCode) {
        return null;
    }
}
