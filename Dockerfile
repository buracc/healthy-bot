FROM openjdk:17 AS build
COPY . /home/healthybot
WORKDIR /home/healthybot
RUN chmod +x gradlew && ./gradlew bootJar --no-daemon

FROM openjdk:17
WORKDIR /home/healthybot
COPY --from=build /home/healthybot/build/libs/*.jar app.jar
ENTRYPOINT java -jar app.jar