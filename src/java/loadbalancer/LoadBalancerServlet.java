package loadbalancer;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class LoadBalancerServlet extends HttpServlet {

    private List<BackendServer> servers;
    private LoadBalancer currentBalancer;
    private final List<String> requestLog = new CopyOnWriteArrayList<>();

    @Override
    public void init() throws ServletException {
        // Initialize backend server pool
        servers = new ArrayList<>();
        servers.add(new BackendServer("Server-A", "192.168.1.10", 8081, 1));
        servers.add(new BackendServer("Server-B", "192.168.1.11", 8082, 2));
        servers.add(new BackendServer("Server-C", "192.168.1.12", 8083, 1));

        currentBalancer = new RoundRobinBalancer(); // default
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String path = req.getPathInfo();
        if (path == null) path = "/";

        PrintWriter out = resp.getWriter();

        switch (path) {
            case "/request":
                handleBalancedRequest(req, resp, out);
                break;
            case "/status":
                handleStatus(out);
                break;
            case "/algorithm":
                handleAlgorithmChange(req, resp, out);
                break;
            case "/toggle":
                handleToggleServer(req, resp, out);
                break;
            case "/log":
                handleLog(out);
                break;
            case "/reset":
                handleReset(out);
                break;
            default:
                resp.setStatus(404);
                out.print("{\"error\":\"Not found\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doGet(req, resp);
    }

    // ── Handlers ─────────────────────────────────────────────────────────────

    private void handleBalancedRequest(HttpServletRequest req,
                                       HttpServletResponse resp,
                                       PrintWriter out) throws IOException {
        BackendServer chosen = currentBalancer.selectServer(servers);

        if (chosen == null) {
            resp.setStatus(503);
            out.print("{\"error\":\"No healthy servers available\"}");
            return;
        }

        String requestId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String result    = chosen.handleRequest(requestId);

        String logEntry = String.format(
            "{\"requestId\":\"%s\",\"algorithm\":\"%s\",\"routedTo\":\"%s\",\"result\":%s}",
            requestId, currentBalancer.getAlgorithmName(), chosen.getId(), result
        );
        requestLog.add(0, logEntry);
        if (requestLog.size() > 50) requestLog.remove(requestLog.size() - 1);

        out.print(logEntry);
    }

    private void handleStatus(PrintWriter out) {
        String serversJson = servers.stream()
            .map(BackendServer::toJson)
            .collect(Collectors.joining(","));

        out.printf("{\"algorithm\":\"%s\",\"servers\":[%s],\"totalLogged\":%d}",
            currentBalancer.getAlgorithmName(), serversJson, requestLog.size());
    }

    private void handleAlgorithmChange(HttpServletRequest req,
                                       HttpServletResponse resp,
                                       PrintWriter out) throws IOException {
        String algo = req.getParameter("type");
        if (algo == null) { resp.setStatus(400); out.print("{\"error\":\"Missing type\"}"); return; }

        switch (algo.toLowerCase()) {
            case "roundrobin":       currentBalancer = new RoundRobinBalancer();       break;
            case "leastconnections": currentBalancer = new LeastConnectionsBalancer(); break;
            case "random":           currentBalancer = new RandomBalancer();            break;
            default:
                resp.setStatus(400);
                out.print("{\"error\":\"Unknown algorithm\"}");
                return;
        }
        out.printf("{\"message\":\"Switched to %s\"}", currentBalancer.getAlgorithmName());
    }

    private void handleToggleServer(HttpServletRequest req,
                                    HttpServletResponse resp,
                                    PrintWriter out) throws IOException {
        String serverId = req.getParameter("id");
        for (BackendServer s : servers) {
            if (s.getId().equals(serverId)) {
                s.setHealthy(!s.isHealthy());
                out.printf("{\"server\":\"%s\",\"healthy\":%b}", s.getId(), s.isHealthy());
                return;
            }
        }
        resp.setStatus(404);
        out.print("{\"error\":\"Server not found\"}");
    }

    private void handleLog(PrintWriter out) {
        out.printf("[%s]", String.join(",", requestLog));
    }

    private void handleReset(PrintWriter out) {
        requestLog.clear();
        servers.forEach(s -> {
            s.setHealthy(true);
        });
        // Re-init servers
        servers.clear();
        servers.add(new BackendServer("Server-A", "192.168.1.10", 8081, 1));
        servers.add(new BackendServer("Server-B", "192.168.1.11", 8082, 2));
        servers.add(new BackendServer("Server-C", "192.168.1.12", 8083, 1));
        out.print("{\"message\":\"Reset complete\"}");
    }
}