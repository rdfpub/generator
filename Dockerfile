# Use this Dockerfile to build an rdfpub site
#
# Use like so:
# docker build -t rdfpub/generator . && docker run \
#   -v /var/run/docker.sock:/var/run/docker.sock \
#   -v /path/to/site/dir:/rdfpub/input:ro \
#   rdfpub/generator \
#   -t rdfpub/example

# Copy rdfpub source and download supporting files
FROM alpine:3.16.0 AS base
COPY . /rdfpub
ENV \
  JETTY_VERSION=9.4.46.v20220331 \
  RDF4J_VERSION=eclipse-rdf4j-4.0.1
RUN apk update \
 && apk add git \
 && wget -O- "https://repo1.maven.org/maven2/org/eclipse/jetty/jetty-runner/$JETTY_VERSION/jetty-runner-$JETTY_VERSION.jar" > /rdfpub/site-build/jetty-runner.jar \
 && wget -O- "https://www.eclipse.org/downloads/download.php?file=/rdf4j/$RDF4J_VERSION-sdk.zip&r=1" | unzip -d /rdfpub/site-build - $RDF4J_VERSION/war/rdf4j-server.war \
 && mv /rdfpub/site-build/$RDF4J_VERSION/war/rdf4j-server.war /rdfpub/site-build/rdf4j-server.war \
 && rm -rf /rdfpub/site-build/$RDF4J_VERSION \
 && git clone --depth=1 https://github.com/EmptyStar/conneg /rdfpub/site-build/conneg \
 && mv /rdfpub/site-build/conneg/*.lua /rdfpub/site-build \
 && rm -rf /rdfpub/site-build/conneg /rdfpub/tests.lua

# Compile and package init process
FROM maven:3.8.5-eclipse-temurin-17-alpine AS init
COPY --from=base /rdfpub/init /rdfpub/init
RUN cd /rdfpub/init \
 && mvn clean package -B -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn

# Minify and package rendering process
FROM node:18.3.0 AS render
COPY --from=base /rdfpub/render /rdfpub/render
RUN cd /rdfpub/render \
 && npm install \
 && npm run bundle \
 && npm run minify

# Configure site container
FROM alpine:3.16.0

# Add necessary packages
RUN apk update \
 && apk add docker-cli-buildx \
 && apk add openjdk17-jre-headless \
 && apk add nodejs-current \
 && apk add brotli \
 && apk add yq

# Copy site generator tools
COPY --from=base /rdfpub/site-build/* /rdfpub/
COPY --from=init /rdfpub/init/target/init.jar /rdfpub/
COPY --from=render /rdfpub/render/render.min.js /rdfpub/

# Run entrypoint script to build the site
ENTRYPOINT ["/rdfpub/entrypoint.sh"]
