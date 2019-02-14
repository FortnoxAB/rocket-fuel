FROM openjdk:9-jre-slim
EXPOSE 8080
COPY ./impl/target/rocket-fuel-*.jar /app.jar
COPY ./impl/config-oncompose.yml /config.yml
CMD ["java", "-jar", "./app.jar", "config.yml"]