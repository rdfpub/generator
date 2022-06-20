# render

This process renders resource HTML pages based on the [Handlebars templates](https://handlebarsjs.com) and [SPARQL query results](https://www.w3.org/TR/sparql11-results-json/) files output by [the init process](../init). It can be run locally with Nodejs and NPM like so:

```sh
npm install
node render.js /path/to/layouts/dir /path/to/resources/dir
```

The `layouts` directory and the `resources` directory illustrated in the example code are meant to have been created by the init process. Once the HTML files are rendered, an rdfpub site can be built from the parent output directory via [the site build process](../site-build).
