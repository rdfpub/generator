# site-build

This process is a Dockerfile and entrypoint script that are used to build an rdfpub site as a Docker image after [the init process](../init) and [the render process](../render) are finished processing the site files. It is not meant to be run locally; it is instead run via [the generator Dockerfile](../Dockerfile). If you do wish to run it locally, you can look at [the entrypoint script](./entrypoint.sh) and adapt it to your local environment.
