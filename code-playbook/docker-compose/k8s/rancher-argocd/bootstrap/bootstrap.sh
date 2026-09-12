#!/usr/bin/env bash
set -euo pipefail

KUBECONFIG_SRC="/kubeconfig/kubeconfig.yaml"
KUBECONFIG_DST="/tmp/kubeconfig.yaml"

echo "[1/8] Waiting for k3s kubeconfig..."

until [ -f "${KUBECONFIG_SRC}" ]; do
  sleep 3
done

cp "${KUBECONFIG_SRC}" "${KUBECONFIG_DST}"

# k3s kubeconfig 내부 server 주소는 기본적으로 127.0.0.1일 수 있음.
# bootstrap 컨테이너에서는 k3s 서비스명으로 접근해야 함.
sed -i 's/https:\/\/127.0.0.1:6443/https:\/\/k3s:6443/g' "${KUBECONFIG_DST}"
sed -i 's/https:\/\/localhost:6443/https:\/\/k3s:6443/g' "${KUBECONFIG_DST}"

export KUBECONFIG="${KUBECONFIG_DST}"

echo "[2/8] Waiting for Kubernetes API..."

until kubectl get nodes >/dev/null 2>&1; do
  sleep 5
done

kubectl get nodes

echo "[3/8] Adding Helm repositories..."

helm repo add jetstack https://charts.jetstack.io
helm repo add rancher-stable https://releases.rancher.com/server-charts/stable
helm repo update

echo "[4/8] Installing cert-manager..."

helm upgrade --install cert-manager jetstack/cert-manager \
  --namespace cert-manager \
  --create-namespace \
  --set crds.enabled=true \
  --wait \
  --timeout 10m

echo "[5/8] Installing Rancher..."

helm upgrade --install rancher rancher-stable/rancher \
  --namespace cattle-system \
  --create-namespace \
  --set hostname="${RANCHER_HOSTNAME}" \
  --set bootstrapPassword="${RANCHER_BOOTSTRAP_PASSWORD}" \
  --set replicas=1 \
  --set ingress.tls.source=rancher \
  --wait \
  --timeout 15m

echo "[6/8] Installing Argo CD..."

kubectl create namespace argocd --dry-run=client -o yaml | kubectl apply -f -

kubectl apply -n argocd \
  -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml

echo "[7/8] Configuring Argo CD ingress..."

kubectl -n argocd patch configmap argocd-cmd-params-cm \
  --type merge \
  -p '{"data":{"server.insecure":"true"}}' || true

kubectl -n argocd patch svc argocd-server \
  --type merge \
  -p '{"spec":{"type":"ClusterIP"}}' || true

kubectl -n argocd rollout restart deployment argocd-server

envsubst < /workspace/argocd-ingress.yaml | kubectl apply -f -

echo "[8/8] Waiting for Argo CD server..."

kubectl -n argocd rollout status deployment argocd-server --timeout=10m

echo ""
echo "=================================================="
echo "Rancher URL : https://${RANCHER_HOSTNAME}"
echo "Rancher ID  : admin"
echo "Rancher PW  : ${RANCHER_BOOTSTRAP_PASSWORD}"
echo ""
echo "Argo CD URL : http://${ARGOCD_HOSTNAME}"
echo "Argo CD ID  : admin"
echo "Argo CD PW  : run below command"
echo ""
echo "docker exec -it rancher-argocd-bootstrap sh -c \\"
echo "  KUBECONFIG=/tmp/kubeconfig.yaml kubectl -n argocd get secret argocd-initial-admin-secret \\"
echo "  -o jsonpath='{.data.password}' | base64 -d && echo"
echo "\\\""
echo "=================================================="