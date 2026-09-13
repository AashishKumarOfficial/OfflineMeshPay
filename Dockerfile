FROM eclipse-temurin:24-jdk AS build

WORKDIR /app

COPY . .

RUN chmod +x mvnw
RUN ./mvnw -DskipTests package

FROM eclipse-temurin:24-jre

WORKDIR /app

COPY --from=build /app/target/OfflineUPI-Mesh-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]