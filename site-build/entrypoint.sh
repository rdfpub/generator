#!/bin/sh

# Stop at first error
set -e

# Input and output directories
BASEDIR=/rdfpub
INPUTDIR=$BASEDIR/input
OUTPUTDIR=$BASEDIR/output

# Generate site
mkdir -p output
java -jar $BASEDIR/init.jar $INPUTDIR $OUTPUTDIR
node $BASEDIR/render.min.js $OUTPUTDIR/layouts $OUTPUTDIR/resources

# Clean up unneeded layout files
rm -rf $OUTPUTDIR/layouts

# Compress resource files
find $OUTPUTDIR/resources \( \
  -name data.jsonld -o \
  -name data.nt -o \
  -name data.rdf -o \
  -name data.ttl -o \
  -name '*.html' \) \
  -exec gzip -9fkv {} \; \
  -exec brotli -9fkv {} \;

# Build Docker image of site
docker buildx build -f /rdfpub/Dockerfile -t $IMAGE_TAG $OUTPUTDIR
