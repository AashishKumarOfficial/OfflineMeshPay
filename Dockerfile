FROM eclipse-temurin:24-jdk

ADD target/OfflineUPI-Mesh-0.0.1-SNAPSHOT.jar OfflineUPI-Mesh-0.0.1-SNAPSHOT.jar

ENTRYPOINT ["java", "-jar", "/OfflineUPI-Mesh-0.0.1-SNAPSHOT.jar"]