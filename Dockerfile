FROM openjdk:17 AS build
COPY . /home/healthybot
WORKDIR /home/healthybot
RUN chmod +x gradlew && ./gradlew bootJar --no-daemon

FROM openjdk:17
COPY --from=build /home/healthybot/build/libs/*.jar /home/healthybot/app/bot.jar
ENTRYPOINT java -jar app/bot.jar