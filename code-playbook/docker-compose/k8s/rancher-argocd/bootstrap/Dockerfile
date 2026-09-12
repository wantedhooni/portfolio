FROM alpine:3.20

RUN apk add --no-cache \
    bash \
    curl \
    ca-certificates \
    openssl \
    git \
    jq

ARG KUBECTL_VERSION=v1.33.1
ARG HELM_VERSION=v3.15.4

RUN curl -fsSL -o /usr/local/bin/kubectl \
    https://dl.k8s.io/release/${KUBECTL_VERSION}/bin/linux/amd64/kubectl \
    && chmod +x /usr/local/bin/kubectl

RUN curl -fsSL https://get.helm.sh/helm-${HELM_VERSION}-linux-amd64.tar.gz \
    | tar -xz \
    && mv linux-amd64/helm /usr/local/bin/helm \
    && chmod +x /usr/local/bin/helm \
    && rm -rf linux-amd64

WORKDIR /workspace

ENTRYPOINT ["/bin/bash", "/workspace/bootstrap.sh"]