FROM quay.io/fortnox/nginx-frontend:1.0.2

ARG CHECKSUM
ENV CHECKSUM=""
ARG ASSET_PATH
ENV ASSET_PATH="/app"

COPY nginx.run nginx.run

COPY build/index.html /www/app/index.html
COPY health.html /www/app/health.html
COPY build/* /www/app/build/
