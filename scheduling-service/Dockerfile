FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

# Copiar o jar já buildado (depois corres mvn clean package)
COPY target/scheduling-service-*.jar app.jar

# Perfil 'postgres' para usar a config application-postgres.properties
ENV SPRING_PROFILES_ACTIVE=postgres

EXPOSE 8084

ENTRYPOINT ["java","-jar","/app/app.jar"]
