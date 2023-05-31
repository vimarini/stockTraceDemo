FROM adoptopenjdk:11-jdk-hotspot AS build

WORKDIR /app

COPY . /app

RUN ./gradlew build

FROM adoptopenjdk:11-jre-hotspot
WORKDIR /app

COPY --from=builder /app/build/libs/stock-trace-0.0.1-SNAPSHOT.jar stock-trace-0.0.1-SNAPSHOT.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]