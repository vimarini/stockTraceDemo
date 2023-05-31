FROM openjdk:17-jdk-slim

WORKDIR /app

COPY . /app

RUN apt-get update && apt-get install -y wget gnupg curl unzip

RUN wget -0 /tmp/chromedriver.zip https://chromedriver.storage

EXPOSE 8080
ADD /build/libs/stock-trace-0.0.1-SNAPSHOT.jar stock-trace-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java", "-jar", "stock-trace-0.0.1-SNAPSHOT.jar"]