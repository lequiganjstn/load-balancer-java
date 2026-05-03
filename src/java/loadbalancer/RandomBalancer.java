package loadbalancer;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class RandomBalancer implements LoadBalancer {

    private final Random random = new Random();

    @Override
    public BackendServer selectServer(List<BackendServer> servers) {
        List<BackendServer> healthy = servers.stream()
            .filter(BackendServer::isHealthy)
            .collect(Collectors.toList());

        if (healthy.isEmpty()) return null;
        return healthy.get(random.nextInt(healthy.size()));
    }

    @Override
    public String getAlgorithmName() { return "Random"; }
}