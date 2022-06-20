# init

This process is the first major component of the rdfpub site generator. It does the heavy lifting of creating the output directory structure, the RDF4J database, the Nginx configurations and most of the static files. It can be built and run locally like so with Java JDK 17 and Maven 2:

```sh
mvn clean package
java -jar target/init.jar /path/to/input/directory /path/to/output/directory
```

The resulting files are expected to be further processed by [the render process](../render).
