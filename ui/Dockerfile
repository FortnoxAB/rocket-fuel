FROM quay.io/fortnox/nginx-frontend:1.0.2

ARG CHECKSUM
ENV CHECKSUM=""
ARG ASSET_PATH
ENV ASSET_PATH="/build"

COPY nginx.run nginx.run

COPY build/index.html /www/index.html
COPY health.html /www/health.html
COPY build/* /www/build/
