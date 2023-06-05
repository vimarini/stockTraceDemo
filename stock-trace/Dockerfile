FROM openjdk:17-jdk-slim

WORKDIR /app
COPY . /app

EXPOSE 8080

ADD /build/libs/stock-trace-0.0.1-SNAPSHOT.jar stock-trace-0.0.1-SNAPSHOT.jar

ENTRYPOINT ["java", "-jar", "stock-trace-0.0.1-SNAPSHOT.jar"]
