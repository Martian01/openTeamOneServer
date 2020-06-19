#!/bin/sh

set -e

# wait for dependencies (as specified by injected environment variables)

/usr/local/bin/wait

# Start the server

exec java -Dspring.config.additional-location=file:/opt/openTeamOneServer/config/ -jar /opt/openTeamOneServer.jar
