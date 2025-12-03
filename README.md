# 🎫 Service Ticket System (srvc-tckt)

<div align="center">

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.0-brightgreen?style=for-the-badge&logo=spring)
![Kubernetes](https://img.shields.io/badge/Kubernetes-Ready-blue?style=for-the-badge&logo=kubernetes)
![License](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)

**A distributed help desk / support ticket system designed to showcase workload management across Kubernetes pods**

[Features](#-features) •
[Quick Start](#-quick-start) •
[Architecture](#-architecture) •
[Deployment](#-kubernetes-deployment) •
[API](#-api-reference) •
[Contributing](#-contributing)

</div>

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Architecture](#-architecture)
- [Technology Stack](#-technology-stack)
- [Quick Start](#-quick-start)
- [Docker](#-docker)
- [Kubernetes Deployment](#-kubernetes-deployment)
- [Configuration](#%EF%B8%8F-configuration)
- [Usage](#-usage)
- [API Reference](#-api-reference)
- [Development](#-development)
- [Testing](#-testing)
- [Monitoring](#-monitoring)
- [Troubleshooting](#-troubleshooting)
- [Contributing](#-contributing)
- [License](#-license)

---

## 🌟 Overview

**srvc-tckt** (Service Ticket) is a full-featured, distributed support ticket management system built with Spring Boot and designed for Kubernetes environments. It demonstrates real-time workload distribution, agent performance tracking, and queue management across multiple pods.

### Key Highlights

- 🚀 **Cloud-Native**: Built for Kubernetes with horizontal scaling
- 🎯 **Priority Management**: Color-coded priority system (Urgent/High/Medium/Low)
- 🔄 **Real-Time Distribution**: Watch tickets flow across pods in real-time
- 🛡️ **Concurrency Safe**: Optimistic locking prevents double-assignment
- 📊 **Metrics & Monitoring**: Built-in actuator endpoints and performance tracking
- 🎨 **Modern UI**: Responsive Thymeleaf templates with JavaScript enhancements

---

## ✨ Features

### Core Functionality

- **📋 Ticket Management**
  - Create, assign, update, and track support tickets
  - Full lifecycle management (NEW → ASSIGNED → IN_PROGRESS → RESOLVED → CLOSED → ARCHIVED)
  - Priority-based queue with visual color coding
  - Category organization (Technical, Billing, Account, General)

- **👥 Multi-Agent Support**
  - Multiple agents can work simultaneously
  - "Assign to Me" functionality with race condition prevention
  - Per-agent performance metrics and dashboards
  - Workload distribution visualization

- **🎯 Priority System**
  - 🔴 **URGENT** - Immediate attention required
  - 🟠 **HIGH** - Important, prompt handling
  - 🟡 **MEDIUM** - Standard priority
  - 🟢 **LOW** - Can be deferred

- **📊 Analytics & Reporting**
  - System-wide statistics dashboard
  - Per-agent performance metrics
  - Pod/Node distribution tracking
  - Response time and resolution time SLA tracking

- **🔔 Notifications**
  - Email notifications for ticket events
  - Async email processing
  - Configurable notification templates

### Kubernetes Features

- **⚖️ Horizontal Scaling**: Auto-scaling based on CPU/Memory metrics
- **🎲 Load Distribution**: Demonstrates workload spread across pods
- **💚 Health Checks**: Liveness and readiness probes
- **📡 Service Discovery**: Pod and node tracking
- **🔄 Rolling Updates**: Zero-downtime deployments
- **💾 Persistent Storage**: PostgreSQL with PVC for data persistence

---

## 🏗️ Architecture

### System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     Load Balancer / Ingress                  │
└───────────────────────┬─────────────────────────────────────┘
                        │
        ┌───────────────┼───────────────┐
        │               │               │
    ┌───▼───┐      ┌───▼───┐      ┌───▼───┐
    │ Pod 1 │      │ Pod 2 │      │ Pod 3 │
    │Agent A│      │Agent B│      │Agent C│
    └───┬───┘      └───┬───┘      └───┬───┘
        │               │               │
        └───────────────┼───────────────┘
                        │
                    ┌───▼────┐
                    │PostgreSQL│
                    └────────┘
```

### Application Components

```
srvc-tckt/
├── controllers/       # HTTP request handlers
├── services/         # Business logic layer
├── repositories/     # Data access layer
├── entities/         # JPA entities
├── dto/             # Data transfer objects
├── config/          # Spring configuration
└── templates/       # Thymeleaf HTML views
```

### Ticket State Flow

```
NEW → ASSIGNED → IN_PROGRESS → RESOLVED → CLOSED → ARCHIVED
         ↓             ↓
         └─────────────┴────→ WAITING_CUSTOMER ──→ IN_PROGRESS
```

---

## 🛠️ Technology Stack

### Backend
- **Java 21** - Modern Java LTS release
- **Spring Boot 4.0.0** - Application framework
- **Spring MVC** - Web framework
- **Spring Data JPA** - Data persistence
- **Spring Security** - Authentication & authorization
- **Hibernate** - ORM
- **Flyway** - Database migrations

### Database
- **H2** - In-memory database (development)
- **PostgreSQL 16** - Production database

### Frontend
- **Thymeleaf** - Server-side templating
- **HTML5/CSS3** - Modern web standards
- **JavaScript (ES6+)** - Client-side interactivity

### DevOps & Infrastructure
- **Docker** - Containerization
- **Kubernetes** - Orchestration
- **Maven** - Build automation
- **Spring Boot Actuator** - Monitoring & metrics

### Additional Tools
- **Lombok** - Boilerplate reduction
- **Spring Boot DevTools** - Development utilities
- **JavaMail** - Email notifications

---

## 🚀 Quick Start

### Prerequisites

- Java 21 or higher
- Maven 3.9+
- Docker (optional, for containerization)
- kubectl & k3s (optional, for Kubernetes deployment)

### Local Development

1. **Clone the repository**
   ```bash
   git clone https://github.com/your-org/srvc-tckt.git
   cd srvc-tckt
   ```

2. **Run with H2 (in-memory database)**
   ```bash
   ./mvnw spring-boot:run
   ```

3. **Access the application**
   ```
   URL: http://localhost:8888
   ```

4. **Login with default credentials**
   - **agent1** / password
   - **agent2** / password
   - **admin** / admin

### Run with PostgreSQL

1. **Start PostgreSQL**
   ```bash
   docker run -d \
     --name postgres \
     -e POSTGRES_DB=srvc_tckt_db \
     -e POSTGRES_USER=postgres \
     -e POSTGRES_PASSWORD=postgres \
     -p 5432:5432 \
     postgres:16-alpine
   ```

2. **Run application with prod profile**
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
   ```

---

## 🐳 Docker

### Build Docker Image

```bash
# Build the image
docker build -t srvc-tckt:latest .

# Verify the image
docker images | grep srvc-tckt
```

### Run with Docker Compose

Create a `docker-compose.yml`:

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: srvc_tckt_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      timeout: 5s
      retries: 5

  srvc-tckt:
    image: srvc-tckt:latest
    depends_on:
      postgres:
        condition: service_healthy
    environment:
      SPRING_PROFILES_ACTIVE: prod
      DB_HOST: postgres
      DB_PORT: 5432
      DB_NAME: srvc_tckt_db
      DB_USERNAME: postgres
      DB_PASSWORD: postgres
    ports:
      - "8888:8888"
    healthcheck:
      test: ["CMD", "wget", "--spider", "http://localhost:8888/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

volumes:
  postgres-data:
```

Run with:
```bash
docker-compose up -d
```

---

## ☸️ Kubernetes Deployment

### Prerequisites

- k3s or any Kubernetes cluster
- kubectl configured
- Docker image built and available

### Quick Deploy to k3s

1. **Build and load image into k3s**
   ```bash
   # Build the image
   docker build -t srvc-tckt:latest .

   # Import into k3s
   docker save srvc-tckt:latest | sudo k3s ctr images import -
   ```

2. **Deploy to Kubernetes**
   ```bash
   kubectl apply -f deployment.yaml
   ```

3. **Check deployment status**
   ```bash
   # Check pods
   kubectl get pods -n srvc-tckt

   # Check services
   kubectl get svc -n srvc-tckt

   # Watch pod distribution
   kubectl get pods -n srvc-tckt -o wide
   ```

4. **Access the application**
   ```bash
   # Get the LoadBalancer IP
   kubectl get svc srvc-tckt-service -n srvc-tckt

   # Access at: http://<EXTERNAL-IP>
   ```

### Scaling

```bash
# Manual scaling
kubectl scale deployment srvc-tckt -n srvc-tckt --replicas=5

# Check HPA status
kubectl get hpa -n srvc-tckt

# Watch auto-scaling in action
kubectl get hpa -n srvc-tckt --watch
```

### Monitoring Pods

```bash
# View logs from all pods
kubectl logs -n srvc-tckt -l app=srvc-tckt --tail=100

# Follow logs from a specific pod
kubectl logs -n srvc-tckt <pod-name> -f

# Execute commands in a pod
kubectl exec -n srvc-tckt <pod-name> -it -- /bin/sh
```

---

## ⚙️ Configuration

### Environment Variables

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `local` | No |
| `SERVER_PORT` | Application port | `8888` | No |
| `DB_HOST` | Database hostname | `localhost` | Yes (prod) |
| `DB_PORT` | Database port | `5432` | Yes (prod) |
| `DB_NAME` | Database name | `srvc_tckt_db` | Yes (prod) |
| `DB_USERNAME` | Database username | `postgres` | Yes (prod) |
| `DB_PASSWORD` | Database password | - | Yes (prod) |
| `HOSTNAME` | Pod name (auto-set by K8s) | `local-dev` | No |
| `NODE_NAME` | Node name (set in deployment) | `local-node` | No |
| `MAIL_HOST` | SMTP host | `localhost` | No |
| `MAIL_PORT` | SMTP port | `1025` | No |

### Application Profiles

- **local** - H2 in-memory database, development features enabled
- **prod** - PostgreSQL database, production optimizations

### Spring Boot Actuator Endpoints

Available at `/actuator`:
- `/health` - Health check
- `/info` - Application info
- `/metrics` - Application metrics
- `/prometheus` - Prometheus metrics

---

## 💡 Usage

### Creating a Ticket

1. Navigate to **Dashboard**
2. Click **Create New Ticket**
3. Fill in the form:
   - Subject
   - Description
   - Priority (Urgent/High/Medium/Low)
   - Category (Technical/Billing/Account/General)
   - Customer Name & Email
4. Click **Submit Ticket**

### Agent Workflow

1. **Login** as an agent (agent1/password)
2. View **Ticket Queue** - See all unassigned tickets
3. Click **Assign to Me** on a ticket
4. Work through the ticket lifecycle:
   - **Start Progress** - Begin working
   - **Request Info** - Ask customer for more details
   - **Resolve** - Mark as resolved
   - **Close** - Final closure

### Monitoring Workload Distribution

1. Go to **Statistics** page
2. View **By Pod** section
3. See how tickets are distributed across pods
4. Compare performance metrics per pod/node

### Keyboard Shortcuts

- `Alt + H` - Dashboard
- `Alt + Q` - Ticket Queue
- `Alt + M` - My Tickets
- `Alt + A` - All Tickets
- `Alt + N` - New Ticket

---

## 📚 API Reference

### REST Endpoints

#### Tickets

```http
GET    /tickets              # List all tickets
GET    /tickets/{id}         # Get ticket details
POST   /tickets/new          # Create ticket
POST   /tickets/{id}/assign  # Assign to current agent
POST   /tickets/{id}/start   # Start progress
POST   /tickets/{id}/resolve # Resolve ticket
POST   /tickets/{id}/close   # Close ticket
```

#### Statistics

```http
GET    /tickets/stats        # System statistics
```

#### Health & Monitoring

```http
GET    /actuator/health      # Health check
GET    /actuator/metrics     # Metrics
GET    /actuator/info        # Application info
```

---

## 🔧 Development

### Building from Source

```bash
# Clean and build
./mvnw clean install

# Skip tests for faster builds
./mvnw clean install -DskipTests

# Run tests only
./mvnw test

# Package as JAR
./mvnw clean package
```

### Project Structure

```
srvc-tckt/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/helpdesk/srvc_tckt/
│   │   │       ├── config/          # Configuration
│   │   │       ├── controller/      # Controllers
│   │   │       ├── dto/            # DTOs
│   │   │       ├── entity/         # Entities
│   │   │       ├── repository/     # Repositories
│   │   │       └── service/        # Services
│   │   └── resources/
│   │       ├── db/migration/       # Flyway migrations
│   │       ├── static/             # CSS, JS
│   │       ├── templates/          # Thymeleaf templates
│   │       └── application.properties
│   └── test/                       # Tests
├── Dockerfile                      # Docker image definition
├── deployment.yaml                 # Kubernetes manifests
├── pom.xml                        # Maven configuration
└── README.md                      # This file
```

### Code Style

- Follow Java naming conventions
- Use Lombok to reduce boilerplate
- Document public APIs with Javadoc
- Write unit tests for service layer
- Keep controllers thin, business logic in services

---

## 🧪 Testing

### Run All Tests

```bash
./mvnw test
```

### Run Specific Test Class

```bash
./mvnw test -Dtest=TicketServiceTest
```

### Test Coverage

```bash
./mvnw clean test jacoco:report
```

View coverage report: `target/site/jacoco/index.html`

---

## 📊 Monitoring

### Prometheus Metrics

Scrape metrics from:
```
http://localhost:8888/actuator/prometheus
```

### Grafana Dashboard

Import the provided dashboard:
- CPU/Memory usage per pod
- Request rate and response times
- Ticket processing metrics
- Database connection pool stats

### Logging

Logs are output to stdout/stderr and can be collected by:
- Kubernetes logs: `kubectl logs`
- Log aggregation: Elasticsearch/Fluentd/Kibana (EFK)
- Cloud logging: CloudWatch, Stackdriver, etc.

---

## 🐛 Troubleshooting

### Application won't start

**Check Java version:**
```bash
java -version  # Should be 21 or higher
```

**Check port availability:**
```bash
lsof -i :8888  # Port should be free
```

### Database connection issues

**PostgreSQL not running:**
```bash
docker ps | grep postgres
```

**Wrong credentials:**
Check `application-prod.properties` and environment variables

### Pods not starting in Kubernetes

**Check pod logs:**
```bash
kubectl logs -n srvc-tckt <pod-name>
```

**Check events:**
```bash
kubectl describe pod -n srvc-tckt <pod-name>
```

**Check resources:**
```bash
kubectl top pods -n srvc-tckt
```

### Common Issues

| Issue | Solution |
|-------|----------|
| 503 Service Unavailable | Database not ready - check init container logs |
| CrashLoopBackOff | Check logs with `kubectl logs` |
| ImagePullBackOff | Ensure image is in k3s: `docker save \| k3s ctr images import` |
| No LoadBalancer IP | k3s uses Traefik - check ingress instead |

---

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. **Fork the repository**
2. **Create a feature branch**
   ```bash
   git checkout -b feature/amazing-feature
   ```
3. **Commit your changes**
   ```bash
   git commit -m "Add amazing feature"
   ```
4. **Push to the branch**
   ```bash
   git push origin feature/amazing-feature
   ```
5. **Open a Pull Request**

### Coding Guidelines

- Write clear commit messages
- Add tests for new features
- Update documentation
- Follow existing code style
- Ensure CI passes

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- Kubernetes community for orchestration tools
- Open source contributors

---

## 📞 Support

- 🐛 Issues: [GitHub Issues](https://github.com/your-org/srvc-tckt/issues)
- 📖 Docs: [Wiki](https://github.com/your-org/srvc-tckt/wiki)

---

<div align="center">

**Built with ❤️ for demonstrating distributed workload management in Kubernetes**

⭐ Star this repo if you find it useful!

</div>
