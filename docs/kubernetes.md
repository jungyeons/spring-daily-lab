# Kubernetes deployment example

The manifests in [`k8s/`](../k8s) create a two-replica API deployment, an internal `ClusterIP` service, and non-sensitive runtime configuration. They use the Actuator liveness and readiness endpoints so a pod only receives traffic after it is ready.

## Prerequisites

Publish an application image and set that immutable image tag in `k8s/deployment.yaml`. Create the database secret in the target namespace; do not put credentials in Git.

```bash
kubectl create secret generic spring-daily-lab-database \
  --from-literal=url='jdbc:postgresql://postgres.example:5432/dailylab' \
  --from-literal=username='dailylab' \
  --from-literal=password='replace-me'
```

## Apply and inspect

```bash
kubectl apply -k k8s/
kubectl rollout status deployment/spring-daily-lab
kubectl get pods -l app.kubernetes.io/name=spring-daily-lab
```

The ConfigMap deliberately disables the H2 console. Database connection values remain references to the Kubernetes Secret. Tune image tags, replica counts, resources, and the service/ingress policy for the target environment.
