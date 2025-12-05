FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

COPY target/Patient-Service-*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=docker

EXPOSE 8083

ENTRYPOINT ["java","-jar","/app/app.jar"]
