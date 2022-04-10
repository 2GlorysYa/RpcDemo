package Client.lb;

import java.util.List;

public interface LoadBalancer {

    /**
     * @param addressList 需要负载均衡的服务列表
     * @return  被选取的服务
     */
    String select(List<String> addressList);

    String select(List<String> addressList, int hashCode);
}
