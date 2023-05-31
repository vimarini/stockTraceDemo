FROM openjdk:11.0.3-jdk AS build
WORKDIR /app

COPY . /app

RUN ./gradlew build

FROM adoptopenjdk:11-jre-hotspot
WORKDIR /app

COPY --from=build /app/build/libs/app.jar /app/app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]