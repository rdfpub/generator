# rdfpub Site Generator

Use the rdfpub site generator to publish your RDF data and supporting files as a static-ish website. RDF + SPARQL queries + [Handlebars](https://www.handlebarsjs.com) templates go in, rendered HTML + RDF documents + a SPARQL endpoint come out, all neatly packed into a lean Docker image that you can run anywhere you can run Docker.

## Features

- Fast site delivery via static web pages with static compression
- Content negotiation for all RDF resources
- Public SPARQL 1.1 endpoint for your RDF data
- Inherent support for i18n
- Deploy your site virtually anywhere with Docker
- Focus on your data with minimal configuration and sensible conventions

## Getting started

If you're totally new to rdfpub, check out [the rdfpub tutorial site](https://github.com/rdfpub/tutorial-site) to learn how to build an rdfpub site by example. If you have a site that you want to build and run, simply use a pre-built generator image like so:

```sh
# Generate a Docker image of your site
docker run                                     \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -v /path/to/your/site/dir:/rdfpub/input:ro   \
  ghcr.io/rdfpub/generator                     \
  -t rdfpub/example

# Run your site
docker run -p 80:80 rdfpub/example
```

And that's it! You can access your site locally for testing, then you can deploy your site image to any environment that supports Docker, including AWS, GCP, Heroku, or other popular hosting providers.

## Problems?

rdfpub is a continuous work-in-progress. If you encounter an error or problem, or if you have suggestions for how rdfpub could do something better, please feel free to [file an issue](https://github.com/rdfpub/generator/issues/new) with as much detail as possible.
