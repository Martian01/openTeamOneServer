#
# Build stage
#
FROM openjdk:8-jdk-alpine AS build
RUN apk add --update --no-cache maven

COPY src /tmp/build/src
COPY docker /tmp/build/docker
COPY pom.xml /tmp/build
RUN mvn -f /tmp/build/pom.xml clean install

#
# Package stage
#
FROM openjdk:8-jre-alpine  
ADD https://github.com/ufoscout/docker-compose-wait/releases/download/2.7.3/wait /usr/local/bin/wait
RUN chmod +x /usr/local/bin/wait
RUN mkdir -p /opt/openTeamOneServer/config && \
mkdir -p /opt/openTeamOneServer/data && \
mkdir -p /opt/openTeamOneServer/keystores
COPY --from=build /tmp/build/docker/start.sh /usr/local/bin/
COPY --from=build /tmp/build/target/*.jar /opt/openTeamOneServer.jar
ENTRYPOINT ["/bin/sh", "-c", "/usr/local/bin/start.sh"]

# use mount point /opt/openTeamOneServer for config injection and server data
