# Use this Dockerfile to build an rdfpub site
#
# Use like so:
# docker build -t rdfpub/generator . && docker run rdfpub/generator \
#   -e IMAGE_TAG=rdfpub/example \
#   -v /var/run/docker.sock:/var/run/docker.sock \
#   -v /path/to/site/dir:/rdfpub/input:ro

# Download rdfpub
FROM alpine:3.16.0 AS base
RUN apk update \
 && apk add git
COPY . /rdfpub

# Compile and package init process
FROM maven:3.8.5-eclipse-temurin-17-alpine AS init
COPY --from=base /rdfpub/init /rdfpub/init
RUN cd /rdfpub/init \
 && mvn clean package

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
 && apk add brotli

# Copy site generator tools
COPY --from=base /rdfpub/site-build/Dockerfile /rdfpub/site-build/entrypoint.sh /rdfpub/
COPY --from=init /rdfpub/init/target/init.jar /rdfpub/
COPY --from=render /rdfpub/render/render.min.js /rdfpub/

# Run entrypoint script to build the site
ENTRYPOINT ["/rdfpub/entrypoint.sh"]
