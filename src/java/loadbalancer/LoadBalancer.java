package loadbalancer;

import java.util.List;

public interface LoadBalancer {
    /** Select the next backend server to route to */
    BackendServer selectServer(List<BackendServer> servers);
    String getAlgorithmName();
}