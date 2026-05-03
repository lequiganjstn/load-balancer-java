# ⚖️ Load Balancer Demo

A web-based demonstration of load balancing algorithms built with
Java Servlets and Apache Ant, deployable on Apache Tomcat.

Visualizes how incoming requests are distributed across multiple
backend servers in real time.

![Java](https://img.shields.io/badge/Java-24-orange)
![Tomcat](https://img.shields.io/badge/Tomcat-10.1-yellow)
![Ant](https://img.shields.io/badge/Build-Ant-blue)
![License](https://img.shields.io/badge/License-MIT-green)

---

## 📸 Features

- **Three load balancing algorithms:**
  - Round Robin — cycles requests evenly across all servers
  - Least Connections — routes to the server with fewest active connections
  - Random — picks a healthy server at random
- **Live server stats** — active connections, total handled, average response time
- **Kill / Revive servers** — simulate server failure and recovery
- **Burst mode** — fire 10 concurrent requests to observe distribution
- **Request log** — see exactly which server handled each request and how fast

---

## 🛠️ Prerequisites

Make sure you have the following installed before proceeding:

| Tool | Version | Download |
|---|---|---|
| JDK | 17 or higher | https://www.oracle.com/java/technologies/downloads/ |
| Apache Tomcat | 10.1.x | https://tomcat.apache.org/download-10.cgi |
| Apache NetBeans | 19 or higher | https://netbeans.apache.org/front/main/download/ |
| Apache Ant | Bundled with NetBeans | — |

> ⚠️ **Tomcat 10.x is required.** Tomcat 9 uses the old `javax.servlet`
> namespace and is not compatible with this project.

---

## ⚙️ Setup

### 1. Clone the repository

```bash
git clone https://github.com/YOUR_USERNAME/LoadBalancerDemo.git
cd LoadBalancerDemo
```

### 2. Download and place Apache Tomcat

Download the **Tomcat 10.1.x Windows zip** from the link above and
extract it. You can place it anywhere — including inside the project
folder, which is the recommended location:
LoadBalancerDemo/
└── apache-tomcat-10.1.x/   ← place it here

### 3. Copy `servlet-api.jar` into the project
FROM: apache-tomcat-10.1.x/lib/servlet-api.jar
TO: LoadBalancerDemo/lib/servlet-api.jar

Create the `lib/` folder if it does not exist.

### 4. Update `build.xml`

Open `build.xml` and update the Tomcat path to match your setup:

```xml
<property name="tomcat.home"
          value="apache-tomcat-10.1.x"/>
```

Replace `apache-tomcat-10.1.x` with your actual Tomcat folder name,
e.g. `apache-tomcat-10.1.54`.

### 5. Set JAVA_HOME (if not already set)

Open Command Prompt and run:

```cmd
where java
```

Note the path (e.g. `C:\Program Files\Java\jdk-24`), then set it
permanently:

1. Press **Windows key** → search **"Environment Variables"**
2. Under **System variables** → **New**
   - Name: `JAVA_HOME`
   - Value: `C:\Program Files\Java\jdk-24`
3. Click **OK** on all dialogs

### 6. Add Tomcat to NetBeans

1. In NetBeans go to **Tools → Servers → Add Server**
2. Select **Apache Tomcat or TomEE**
3. Set **Server Location** to your Tomcat folder
4. Enter any username and password
5. Leave **"Create user if it does not exist"** checked
6. Click **Finish**

---

## ▶️ Running the App

### Option A — Via NetBeans (recommended)

1. Open NetBeans → **File → Open Project** → select the
   `LoadBalancerDemo` folder
2. Start Tomcat: **Services tab → Servers → Apache Tomcat → Start**
3. Right-click `build.xml` → **Run Target → dist**
4. Copy `dist/LoadBalancerDemo.war` into
   `apache-tomcat-10.1.x/webapps/`
5. Open your browser:
http://localhost:8080/LoadBalancerDemo/

### Option B — Via Command Prompt

```cmd
cd apache-tomcat-10.1.x\bin
set JAVA_HOME=C:\Program Files\Java\jdk-24
startup.bat
```

Then build and deploy:

```cmd
cd ..\..\
ant dist
copy dist\LoadBalancerDemo.war apache-tomcat-10.1.x\webapps\
```

Open your browser at `http://localhost:8080/LoadBalancerDemo/`

---

## 📁 Project Structure
LoadBalancerDemo/
├── build.xml                          # Ant build script
├── lib/
│   └── servlet-api.jar                # Tomcat servlet API (you add this)
├── src/
│   └── java/
│       └── loadbalancer/
│           ├── BackendServer.java     # Simulated backend server
│           ├── LoadBalancer.java      # Balancer interface
│           ├── RoundRobinBalancer.java
│           ├── LeastConnectionsBalancer.java
│           ├── RandomBalancer.java
│           └── LoadBalancerServlet.java  # Main servlet / REST API
└── web/
├── WEB-INF/
│   └── web.xml                    # Servlet configuration
└── index.html                     # Frontend UI

---

## 🔌 API Endpoints

All endpoints are under `/api/`:

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/request` | Send a request through the load balancer |
| GET | `/api/status` | Get all server stats |
| GET | `/api/algorithm?type=` | Switch algorithm (`roundrobin`, `leastconnections`, `random`) |
| GET | `/api/toggle?id=` | Toggle a server healthy/unhealthy |
| GET | `/api/log` | Get the last 50 request log entries |
| GET | `/api/reset` | Reset all stats and server states |

---

## 🧠 How It Works

The servlet (`LoadBalancerServlet`) maintains a pool of three simulated
backend servers. Each server has a different artificial response delay
to mimic real-world variance:

| Server | Simulated Base Latency |
|---|---|
| Server-A | ~100ms |
| Server-B | ~200ms |
| Server-C | ~150ms |

When a request hits `/api/request`, the active load balancing algorithm
selects a server, the server "processes" the request (with the simulated
delay), and the result is returned to the UI along with routing metadata.

---

## 🐛 Troubleshooting

**`package jakarta.servlet does not exist`**
→ `lib/servlet-api.jar` is missing. See Setup Step 3.

**`localhost refused to connect`**
→ Tomcat is not running. See Running the App.

**`HTTP 404 – Not Found`**
→ WAR file not deployed. Copy `dist/LoadBalancerDemo.war` into
`apache-tomcat-10.1.x/webapps/` and restart Tomcat.

**`JAVA_HOME is not defined`**
→ See Setup Step 5.

---

## 📄 License

MIT License — free to use, modify, and distribute.
