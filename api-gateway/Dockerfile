FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

COPY target/api-gateway-*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=docker

EXPOSE 8086

ENTRYPOINT ["java","-jar","/app/app.jar"]
