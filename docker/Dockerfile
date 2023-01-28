ARG JDK_IMG=eclipse-temurin
ARG JDK_VER=17-jdk-alpine
ARG MAVEN_VER=3.8.7-eclipse-temurin-17-alpine
# -=-=-=-=-=-=-=-=-=-=-
FROM maven:$MAVEN_VER AS build

ARG COMPILE=false  # if false, fetch from repo
# TODO workout correct URL for a specific version
#ARG GC_VER="1.0-SNAPSHOT"
#ARG GC_URL="https://repo.opencollab.dev/artifactory/maven-snapshots/org/geysermc/geyser-connect/$GC_VER/geyser-connect-${GC_VER}.jar"
ARG GC_VER="latest"
ARG GC_URL="https://ci.opencollab.dev/job/GeyserMC/job/GeyserConnect/job/master/lastSuccessfulBuild/artifact/target/GeyserConnect.jar"

WORKDIR /target
COPY . .
RUN [ "$COMPILE" = "true" ] \
 && (echo "Compiling from source..." ; mvn package ) \
 || wget -O "/target/geyser-connect-${GC_VER}.jar" "${GC_URL}"
# Result is in /target/geyser-connect-(...).jar

# -=-=-=-=-=-=-=-=-=-=-
FROM $JDK_IMG:$JDK_VER

WORKDIR /app
COPY --from=build /target/geyser-connect-*.jar GeyserConnect.jar

WORKDIR /gsc
VOLUME ["/gsc"]
# do not run as root
USER nobody
EXPOSE 19132/udp
CMD ["java", "-Xms1G", "-jar", "/app/GeyserConnect.jar"]
