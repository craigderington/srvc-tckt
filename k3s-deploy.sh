#!/bin/bash
# K3s Deployment Script for srvc-tckt

set -e

echo "=================================================="
echo "  Service Ticket System - K3s Deployment"
echo "=================================================="

# Step 1: Import Docker image into k3s
echo ""
echo "Step 1: Importing Docker image into k3s..."
docker save srvc-tckt:latest | sudo k3s ctr images import -

# Step 2: Verify image import
echo ""
echo "Step 2: Verifying image import..."
sudo k3s ctr images ls | grep srvc-tckt

# Step 3: Deploy to Kubernetes
echo ""
echo "Step 3: Deploying to Kubernetes..."
kubectl apply -f deployment.yaml

# Step 4: Wait for pods to be ready
echo ""
echo "Step 4: Waiting for pods to be ready..."
kubectl wait --for=condition=ready pod -l app=postgres -n srvc-tckt --timeout=120s
kubectl wait --for=condition=ready pod -l app=srvc-tckt -n srvc-tckt --timeout=180s

# Step 5: Get deployment status
echo ""
echo "Step 5: Deployment Status"
echo "=========================="
kubectl get all -n srvc-tckt

# Step 6: Get service endpoint
echo ""
echo "Step 6: Access Information"
echo "=========================="
echo "Service endpoint:"
kubectl get svc srvc-tckt-service -n srvc-tckt

echo ""
echo "Ingress information:"
kubectl get ingress -n srvc-tckt

echo ""
echo "=================================================="
echo "  Deployment Complete!"
echo "=================================================="
echo ""
echo "Useful commands:"
echo "  - View logs:     kubectl logs -n srvc-tckt -l app=srvc-tckt --tail=100"
echo "  - Watch pods:    kubectl get pods -n srvc-tckt -w"
echo "  - Scale up:      kubectl scale deployment srvc-tckt -n srvc-tckt --replicas=5"
echo "  - Port forward:  kubectl port-forward -n srvc-tckt svc/srvc-tckt-service 8888:80"
echo ""
