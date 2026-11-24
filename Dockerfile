# ---- build stage ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q clean package -DskipTests

# ---- runtime stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /build/target/url-shortener-1.0.0.jar app.jar
VOLUME /app/data
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
