FROM openjdk:17 AS build
COPY . /home/healthybot
WORKDIR /home/healthybot
RUN \
   chmod +x gradlew && \
    ./ gradlew bootJar

FROM openjdk:17
COPY --from=build /home/healthybot/build/libs/*.jar bot.jar
ENTRYPOINT java -jar bot.jar