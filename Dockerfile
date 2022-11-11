FROM openjdk:17 AS build
COPY . /home/healthybot
WORKDIR /home/healthybot
RUN chmod +x gradlew && ./gradlew bootJar --no-daemon

FROM openjdk:17
COPY --from=build build/libs/*.jar /home/healthybot/app/bot.jar
WORKDIR /home/healthybot/app
ENTRYPOINT java -jar bot.jar