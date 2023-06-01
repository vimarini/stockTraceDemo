FROM openjdk:17-jdk-slim

# Install necessary dependencies
RUN apt-get update && apt-get install -y wget curl gnupg

# Add Google Chrome repository key
RUN wget -q -O - https://dl-ssl.google.com/linux/linux_signing_key.pub | apt-key add -

# Add Google Chrome repository
RUN echo "deb [arch=amd64] http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google-chrome.list

# Update package lists and install Google Chrome
RUN apt-get update && apt-get install -y google-chrome-stable

# Download and install ChromeDriver
RUN CHROME_DRIVER_VERSION=$(curl -sS chromedriver.storage.googleapis.com/LATEST_RELEASE) && \
    wget -O /tmp/chromedriver.zip "https://chromedriver.storage.googleapis.com/$CHROME_DRIVER_VERSION/chromedriver_linux64.zip" && \
    unzip /tmp/chromedriver.zip -d /usr/local/bin/

WORKDIR /app

COPY . /app

EXPOSE 8080

ADD /build/libs/stock-trace-0.0.1-SNAPSHOT.jar stock-trace-0.0.1-SNAPSHOT.jar

ENTRYPOINT ["java", "-jar", "stock-trace-0.0.1-SNAPSHOT.jar"]
