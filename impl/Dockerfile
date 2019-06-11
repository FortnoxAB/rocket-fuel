FROM openjdk:11-jre AS build-env
ENV DOCKERIZE_VERSION v0.6.0
RUN wget https://github.com/jwilder/dockerize/releases/download/$DOCKERIZE_VERSION/dockerize-alpine-linux-amd64-$DOCKERIZE_VERSION.tar.gz \
    && tar -C /usr/local/bin -xzvf dockerize-alpine-linux-amd64-$DOCKERIZE_VERSION.tar.gz \
    && rm dockerize-alpine-linux-amd64-$DOCKERIZE_VERSION.tar.gz


FROM openjdk:11-jre-slim

COPY --from=build-env /usr/local/bin/dockerize /usr/local/bin/dockerize
EXPOSE 8080
COPY ./target/rocket-fuel-fatjar.jar /app.jar
COPY ./config.deploy.yml.tmpl /config.yml.tmpl

CMD /usr/local/bin/dockerize -template config.yml.tmpl:config.yml && java -Xmx200m -Xss256k -XX:MaxMetaspaceSize=100m -XX:MaxDirectMemorySize=128m -XX:CompressedClassSpaceSize=10m -XX:ReservedCodeCacheSize=20m -XX:MinHeapFreeRatio=50 -XX:MaxHeapFreeRatio=50 -XX:+HeapDumpOnOutOfMemoryError -XX:-ShrinkHeapInSteps -XX:MaxGCPauseMillis=10 -jar app.jar db-migrate-run config.yml
