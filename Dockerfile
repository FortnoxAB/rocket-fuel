FROM openjdk:11-jre-slim
EXPOSE 8080
COPY ./impl/target/rocket-fuel-*.jar /app.jar
CMD ["java", "-jar", "./app.jar", "config.yml"]
