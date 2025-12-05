FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

COPY target/physician-service-*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=docker

EXPOSE 8081

ENTRYPOINT ["java","-jar","/app/app.jar"]