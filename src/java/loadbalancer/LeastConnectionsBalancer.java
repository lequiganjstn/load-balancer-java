package loadbalancer;

import java.util.Comparator;
import java.util.List;

public class LeastConnectionsBalancer implements LoadBalancer {

    @Override
    public BackendServer selectServer(List<BackendServer> servers) {
        return servers.stream()
            .filter(BackendServer::isHealthy)
            .min(Comparator.comparingInt(BackendServer::getActiveConnections))
            .orElse(null);
    }

    @Override
    public String getAlgorithmName() { return "Least Connections"; }
}