package Client.cache;

import Server.Service.Service;
import Server.Service.ServiceA;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务发现本地缓存
 */
public class ServerDiscoveryCache {

    private static final Map<String, List<String>> SERVER_MAP = new ConcurrentHashMap<>();

    // 客户端拉取的远程服务的service class
    public static final List<String> SERVICE_CLASS_NAMES = new ArrayList<>();

    public static void put(String serviceName, List<String> serviceList) {
        SERVER_MAP.put(serviceName, serviceList);
    }

    public static void remove(String serviceName, Service service) {
        SERVER_MAP.computeIfPresent(serviceName, (key, value) -> {
            return null;
        });
    }

    public static boolean isEmpty(String serviceName) {
        return SERVER_MAP.get(serviceName) == null || SERVER_MAP.get(serviceName).size() == 0;
    }

    public static List<String> get(String serviceName) {
        return SERVER_MAP.get(serviceName);
    }
}
