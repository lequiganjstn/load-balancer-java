package loadbalancer;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class BackendServer {

    private final String id;
    private final String host;
    private final int port;
    private final int weight;
    private boolean healthy;

    private final AtomicInteger activeConnections = new AtomicInteger(0);
    private final AtomicLong totalRequestsHandled = new AtomicLong(0);
    private final AtomicLong totalResponseTimeMs = new AtomicLong(0);

    public BackendServer(String id, String host, int port, int weight) {
        this.id = id;
        this.host = host;
        this.port = port;
        this.weight = weight;
        this.healthy = true;
    }

    /** Simulate handling a request with artificial latency */
    public String handleRequest(String requestId) {
        activeConnections.incrementAndGet();
        long start = System.currentTimeMillis();

        try {
            // Simulate variable processing time per server
            long delay = simulateProcessing();
            long elapsed = System.currentTimeMillis() - start;
            totalResponseTimeMs.addAndGet(elapsed);
            totalRequestsHandled.incrementAndGet();

            return String.format(
                "{\"server\":\"%s\",\"host\":\"%s:%d\",\"requestId\":\"%s\"," +
                "\"processingMs\":%d,\"activeConnections\":%d,\"totalHandled\":%d}",
                id, host, port, requestId, elapsed,
                activeConnections.get(), totalRequestsHandled.get()
            );
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "{\"error\":\"interrupted\"}";
        } finally {
            activeConnections.decrementAndGet();
        }
    }

    private long simulateProcessing() throws InterruptedException {
        // Each server has a different "speed profile"
        long baseDelay;
        switch (id) {
            case "Server-A": baseDelay = 100; break;
            case "Server-B": baseDelay = 200; break;
            case "Server-C": baseDelay = 150; break;
            default:         baseDelay = 120; break;
        }
        long jitter = (long)(Math.random() * 80);
        Thread.sleep(baseDelay + jitter);
        return baseDelay + jitter;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public String getId()               { return id; }
    public String getHost()             { return host; }
    public int    getPort()             { return port; }
    public int    getWeight()           { return weight; }
    public boolean isHealthy()          { return healthy; }
    public void   setHealthy(boolean h) { this.healthy = h; }
    public int    getActiveConnections(){ return activeConnections.get(); }
    public long   getTotalRequestsHandled() { return totalRequestsHandled.get(); }

    public double getAverageResponseTimeMs() {
        long total = totalRequestsHandled.get();
        return total == 0 ? 0 : (double) totalResponseTimeMs.get() / total;
    }

    public String toJson() {
        return String.format(
            "{\"id\":\"%s\",\"host\":\"%s\",\"port\":%d,\"weight\":%d," +
            "\"healthy\":%b,\"activeConnections\":%d,\"totalHandled\":%d,\"avgResponseMs\":%.1f}",
            id, host, port, weight, healthy,
            activeConnections.get(), totalRequestsHandled.get(),
            getAverageResponseTimeMs()
        );
    }
}