FROM quay.io/fortnox/nginx-frontend:1.0.2

ARG CHECKSUM
ENV CHECKSUM=""
ARG ASSET_PATH
ENV ASSET_PATH="/rocket-fuel/build"

COPY nginx.run nginx.run

COPY build/index.html /www/rocket-fuel/index.html
COPY build/index.html /www/index.html
COPY health.html /www/rocket-fuel/health.html
COPY build/* /www/rocket-fuel/build/
