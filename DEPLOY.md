# Deployment Guide for srvc-tckt

## Current Status

✅ **Docker image built successfully**: `srvc-tckt:latest` (345MB)

## Quick Deploy to k3s

### Prerequisites

Install k3s if not already installed:

```bash
# Install k3s
curl -sfL https://get.k3s.io | sh -

# Verify installation
sudo k3s kubectl get nodes

# Set up kubectl access (optional, for non-root access)
mkdir ~/.kube
sudo cp /etc/rancher/k3s/k3s.yaml ~/.kube/config
sudo chown $USER:$USER ~/.kube/config
export KUBECONFIG=~/.kube/config
```

### Deploy Application

#### Option 1: Automated Deployment (Recommended)

```bash
./k3s-deploy.sh
```

This script will:
1. Import the Docker image into k3s
2. Deploy all Kubernetes resources
3. Wait for pods to be ready
4. Display deployment status and access information

#### Option 2: Manual Deployment

```bash
# 1. Import image into k3s
docker save srvc-tckt:latest | sudo k3s ctr images import -

# 2. Verify image
sudo k3s ctr images ls | grep srvc-tckt

# 3. Deploy to Kubernetes
kubectl apply -f deployment.yaml

# 4. Check deployment
kubectl get pods -n srvc-tckt -w
```

## Access the Application

### Method 1: LoadBalancer Service (Traefik)

```bash
# Get the service endpoint
kubectl get svc srvc-tckt-service -n srvc-tckt

# Access at the EXTERNAL-IP shown (usually localhost or node IP)
# http://<EXTERNAL-IP>
```

### Method 2: Port Forward (Development)

```bash
kubectl port-forward -n srvc-tckt svc/srvc-tckt-service 8080:80
```

Then access at: http://localhost:8080

### Method 3: Ingress (Production)

```bash
# Add to /etc/hosts
echo "127.0.0.1 srvc-tckt.local" | sudo tee -a /etc/hosts

# Access at
# http://srvc-tckt.local
```

## Default Credentials

- **agent1** / password
- **agent2** / password
- **admin** / admin

## Monitoring

### View Logs

```bash
# All pods
kubectl logs -n srvc-tckt -l app=srvc-tckt --tail=100

# Specific pod
kubectl logs -n srvc-tckt <pod-name> -f

# PostgreSQL logs
kubectl logs -n srvc-tckt -l app=postgres
```

### Check Pod Distribution

```bash
# See which nodes pods are running on
kubectl get pods -n srvc-tckt -o wide

# Watch in real-time
watch -n 2 kubectl get pods -n srvc-tckt -o wide
```

### Health Checks

```bash
# Check health endpoint
kubectl exec -n srvc-tckt <pod-name> -- wget -qO- http://localhost:8080/actuator/health

# Check metrics
kubectl exec -n srvc-tckt <pod-name> -- wget -qO- http://localhost:8080/actuator/metrics
```

## Scaling

### Manual Scaling

```bash
# Scale to 5 replicas
kubectl scale deployment srvc-tckt -n srvc-tckt --replicas=5

# Verify
kubectl get pods -n srvc-tckt
```

### Auto-scaling (HPA)

```bash
# Check HPA status
kubectl get hpa -n srvc-tckt

# Watch auto-scaling
kubectl get hpa -n srvc-tckt --watch

# Describe HPA details
kubectl describe hpa srvc-tckt-hpa -n srvc-tckt
```

## Testing Workload Distribution

1. **Access the dashboard**: http://<service-endpoint>
2. **Login as agent1**: agent1 / password
3. **Create multiple tickets**: Use the "New Ticket" form
4. **View Statistics**: Check the "By Pod" section to see ticket distribution
5. **Login as agent2** (different browser/incognito): See tickets distributed across pods
6. **Scale up pods**: `kubectl scale deployment srvc-tckt -n srvc-tckt --replicas=5`
7. **Observe distribution**: Watch how new tickets get assigned to different pods

## Troubleshooting

### Pods Not Starting

```bash
# Check pod status
kubectl get pods -n srvc-tckt

# Describe pod for events
kubectl describe pod -n srvc-tckt <pod-name>

# Check logs
kubectl logs -n srvc-tckt <pod-name>
```

### Database Connection Issues

```bash
# Check PostgreSQL pod
kubectl get pods -n srvc-tckt -l app=postgres

# Check PostgreSQL logs
kubectl logs -n srvc-tckt -l app=postgres

# Verify service
kubectl get svc postgres-service -n srvc-tckt
```

### Image Pull Issues

If you see `ImagePullBackOff`, the image isn't in k3s:

```bash
# Re-import the image
docker save srvc-tckt:latest | sudo k3s ctr images import -

# Verify
sudo k3s ctr images ls | grep srvc-tckt

# Restart deployment
kubectl rollout restart deployment srvc-tckt -n srvc-tckt
```

### Clean Up and Redeploy

```bash
# Delete all resources
kubectl delete namespace srvc-tckt

# Redeploy
kubectl apply -f deployment.yaml
```

## Next Steps

After deployment:

1. **Test the application** - Create tickets, assign them, track distribution
2. **Monitor metrics** - Use the /actuator/prometheus endpoint for Prometheus/Grafana
3. **Load test** - Generate traffic to trigger HPA auto-scaling
4. **Customize** - Update ConfigMap for mail server, adjust resource limits, etc.

## Production Considerations

Before production deployment:

1. **Change secrets** - Update database credentials in `deployment.yaml`
2. **Configure email** - Set real SMTP server in ConfigMap
3. **Set domain** - Update ingress host from `srvc-tckt.local`
4. **Add TLS** - Configure cert-manager for HTTPS
5. **Adjust resources** - Tune CPU/memory limits based on load
6. **Enable monitoring** - Deploy Prometheus/Grafana stack
7. **Configure backups** - Set up PostgreSQL backup strategy
