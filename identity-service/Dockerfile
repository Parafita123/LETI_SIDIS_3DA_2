FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

COPY target/identity-service-*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=docker

EXPOSE 8085

ENTRYPOINT ["java","-jar","/app/app.jar"]
