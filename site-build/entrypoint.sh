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
node $BASEDIR/render.min.js $OUTPUTDIR/layouts $OUTPUTDIR/resources "`yq '.BaseURI' $INPUTDIR/.rdfpub`"

# Clean up unneeded layout files
rm -rf $OUTPUTDIR/layouts

# Compress resource files and user-specified files
compress='(/data\.jsonld$)|(/data\.nt$)|(/data\.rdf$)|(/data\.ttl$)|(/index@[^.]+\.html$)'
for regex in `yq -o p '.CompressFiles' $INPUTDIR/.rdfpub | sed -r 's/(^[0-9]+ = )//g;s/\\\\\\\\/\\\\/g'`; do
  echo "CompressFiles: compressing files matching pattern $regex"
  compress="$compress|($regex)"
done
for file in `find $OUTPUTDIR/resources | grep -E "$compress"`; do
  gzip -9fkv "$file"
  brotli -9fkv "$file"
done

# Build Docker image of site
docker buildx build -f /rdfpub/Dockerfile "$@" $BASEDIR
