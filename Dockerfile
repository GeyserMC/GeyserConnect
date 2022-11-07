FROM maven:latest AS build
COPY . .
RUN mvn package
# Result is in /target/geyser-connect-(...).jar

FROM openjdk:15-alpine
COPY --from=build target/geyser-connect-*.jar geyser-connect.jar
CMD java -jar geyser-connect.jar