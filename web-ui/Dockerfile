FROM node:20-alpine AS builder
ARG BUILD_MODE=local

WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .

RUN ls -al
RUN echo "${BUILD_MODE}"
RUN npm run build -- --mode ${BUILD_MODE}
RUN echo "=== dist ===" \
 && ls -al /app/dist \
 && echo "=== assets ===" \
 && ls -al /app/dist/assets | head -20

FROM nginx:1.25-alpine
RUN ls -al

COPY --from=builder /app/dist/ /usr/share/nginx/html/
COPY nginx.conf /etc/nginx/conf.d/default.conf

RUN ls -al /usr/share/nginx/html/

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD wget -qO- http://localhost/ > /dev/null || exit 1
