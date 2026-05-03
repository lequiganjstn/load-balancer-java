package loadbalancer;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class RoundRobinBalancer implements LoadBalancer {

    private final AtomicInteger counter = new AtomicInteger(0);

    @Override
    public BackendServer selectServer(List<BackendServer> servers) {
        List<BackendServer> healthy = servers.stream()
            .filter(BackendServer::isHealthy)
            .collect(Collectors.toList());

        if (healthy.isEmpty()) return null;

        int index = counter.getAndIncrement() % healthy.size();
        return healthy.get(index);
    }

    @Override
    public String getAlgorithmName() { return "Round Robin"; }
}