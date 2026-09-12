https://github.com/spring-guides/top-spring-on-kubernetes
내용 실습



```
# minikube 사용이면
eval $(minikube docker-env)

#deployment 생성
kubectl create deployment gs-spring-boot-k8s --image spring-k8s/spring-k8s/hello-spring-k8s:latest -o yaml --dry-run=client > deployment.yaml

#service 생성
kubectl create service clusterip gs-spring-boot-k8s --tcp 80:8080 -o yaml --dry-run=client > service.yaml


# 폴더 전체 적용
kubectl apply -R -f ./k8s/ 

kubectl port-forward  svc/gs-spring-boot-k8s  9090:80
```