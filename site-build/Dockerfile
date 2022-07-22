# syntax=docker/dockerfile:1.3-labs
# Dockerfile to build an image for a compiled and rendered rdfpub site
FROM alpine:3.16.0
COPY output /rdfpub
COPY jetty-runner.jar rdf4j-server.war *.lua /tmp

# Set environment variables
ENV \

# Set Jetty environment variables
  JETTY_HOME=/rdfpub/jetty \
  JETTY_SERVICE=/etc/service/jetty \
  JETTY_USER=jetty \

# Set RDF4J environment variables
  RDF4J_VERSION=eclipse-rdf4j-4.0.1 \
  RDF4J_HOME=/rdfpub/rdf4j \
  RDF4J_USER=rdf4j \

# Set OpenResty environment variables
  OPENRESTY_HOME=/rdfpub/nginx \
  OPENRESTY_SERVICE=/etc/service/openresty \
  OPENRESTY_USER=openresty

RUN <<DOCKERRUN
# Basic container configuration
set -e

adduser -D $JETTY_USER
adduser -D $RDF4J_USER
adduser -D $OPENRESTY_USER

apk update
apk add openjdk17-jre-headless

# Install and configure Jetty
mkdir -p $JETTY_HOME
chown -R $JETTY_USER:$JETTY_USER $JETTY_HOME
mv /tmp/jetty-runner.jar $JETTY_HOME/

# Install and configure RDF4J
mkdir -p $RDF4J_HOME $RDF4J_HOME/server/conf $RDF4J_HOME/server/logs
mv /tmp/rdf4j-server.war $JETTY_HOME/
rm -rf $RDF4J_HOME/$RDF4J_VERSION
cat <<LOGBACK > $RDF4J_HOME/server/conf/logback.xml
<configuration>
  <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
    <layout class="ch.qos.logback.classic.PatternLayout">
      <Pattern>%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n</Pattern>
    </layout>
  </appender>
  <logger name="pub.rdf" level="info" additivity="false">
    <appender-ref ref="CONSOLE"/>
  </logger>
  <root level="info">
      <appender-ref ref="CONSOLE"/>
  </root>
</configuration>
LOGBACK
mkdir -p $RDF4J_HOME/server/repositories/rdfpub
mv /rdfpub/db/* $RDF4J_HOME/server/repositories/rdfpub/
rm -rf /rdfpub/db/
chown -R $RDF4J_USER:$JETTY_USER $RDF4J_HOME
chmod ug+w -R $RDF4J_HOME
chmod a-w \
  $RDF4J_HOME/server/repositories/rdfpub/config.ttl \
  $RDF4J_HOME/server/repositories/rdfpub/contexts.dat \
  $RDF4J_HOME/server/repositories/rdfpub/namespaces.dat \
  $RDF4J_HOME/server/repositories/rdfpub/nativerdf.ver \
  $RDF4J_HOME/server/repositories/rdfpub/triples.prop \
  $RDF4J_HOME/server/repositories/rdfpub/txncache.*

# Install and configure OpenResty
wget -O- 'https://openresty.org/package/admin@openresty.com-5ea678a6.rsa.pub' > /etc/apk/keys/admin@openresty.com-5ea678a6.rsa.pub
echo "https://openresty.org/package/alpine/v3.15/main" >> /etc/apk/repositories
apk add openresty
mkdir -p $OPENRESTY_HOME/conneg
mv /tmp/*.lua $OPENRESTY_HOME/conneg
chmod a-w -R $OPENRESTY_HOME/conneg $OPENRESTY_HOME/*.conf
chown -R $OPENRESTY_USER:$OPENRESTY_USER $OPENRESTY_HOME

# Install and configure runit
apk add runit
mkdir -p /etc/runit

# Add Jetty runit service
mkdir -p $JETTY_SERVICE
cat <<JETTYRUN > $JETTY_SERVICE/run
#!/bin/sh
exec chpst -u $JETTY_USER java -Dorg.eclipse.rdf4j.appdata.basedir=$RDF4J_HOME -jar $JETTY_HOME/jetty-runner.jar --host 127.0.0.1 $JETTY_HOME/rdf4j-server.war 1>/proc/1/fd/1 2>/proc/1/fd/2
JETTYRUN

# Add OpenResty runit service
mkdir -p $OPENRESTY_SERVICE
mkdir -p $OPENRESTY_HOME/logs
chown -R $OPENRESTY_USER:$OPENRESTY_USER $OPENRESTY_HOME/logs
cat <<OPENRESTYRUN > $OPENRESTY_SERVICE/run
#!/bin/sh
export LUA_PATH="$LUA_PATH;$OPENRESTY_HOME/conneg/?.lua"
chown $OPENRESTY_USER:$OPENRESTY_USER /dev/stdout /dev/stderr
exec chpst -u $OPENRESTY_USER openresty -p $OPENRESTY_HOME -c $OPENRESTY_HOME/nginx.conf 1>/proc/1/fd/1 2>/proc/1/fd/2
OPENRESTYRUN

# Make runit services executable
chmod +x $JETTY_SERVICE/run $OPENRESTY_SERVICE/run

DOCKERRUN

EXPOSE 8081

ENTRYPOINT ["/sbin/runsvdir","/etc/service"]
