FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

COPY target/clinical-records-service-*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=docker

EXPOSE 8082

ENTRYPOINT ["java","-jar","/app/app.jar"]
