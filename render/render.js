const fs = require('fs');
const Handlebars = require('handlebars');
const MarkdownIt = require('markdown-it');

// Get processing directories
const INPUTDIR = process.argv[2];
const OUTPUTDIR = process.argv[3];

console.log(`Input directory is ${INPUTDIR}`);
console.log(`Output directory is ${OUTPUTDIR}`);

// Convenience function for path appending
const subpath = (dir, segment) => {
  return dir + '/' + segment;
};

// Decorate SPARQL query results with convenience values
const decorateSPARQLResults = function (rq) {
  // {{# each query }}...{{/ each }}
  rq[Symbol.iterator] = rq.results.bindings[Symbol.iterator].bind(
    rq.results.bindings
  );

  // Decorate object only if it has results
  if (rq.results.bindings.length) {
    // {{ query.var }}
    rq.head.vars.forEach((v) => {
      rq[v] ??= {};
      rq[v].toString = () => rq.results.bindings[0][v]?.value ?? '';
    });

    // {{# each query }}{{ value }}{{/ each }}
    rq.results.bindings.forEach((binding) => {
      Object.entries(binding).forEach(([k, v]) => {
        v.toString = () => v?.value ?? '';
      });
    });
  }
  return rq;
};

// Extend isEmpty to look at string values and identify queries without results as empty
const ogie = Handlebars.Utils.isEmpty;
Handlebars.Utils.isEmpty = (o) => {
  return typeof o === 'object'
    ? !o.toString() ||
        (o[Symbol.iterator] && o[Symbol.iterator]().next().value === undefined)
    : ogie(o);
};

// Define Handlebars helpers
const md = new MarkdownIt({
  html: true,
  linkify: true,
});
const helpers = [
  {
    // Logical OR: {{# if (any foo bar baz) }}
    name: 'any',
    fn: (...conditions) => {
      return conditions.slice(0, -1).some((e) => !Handlebars.Utils.isEmpty(e));
    },
  },
  {
    // Logical AND: {{# if (all foo bar baz) }}
    name: 'all',
    fn: (...conditions) => {
      return conditions.slice(0, -1).every((e) => !Handlebars.Utils.isEmpty(e));
    },
  },
  {
    // Markdown: {{{markdown query.description}}}
    name: 'markdown',
    fn: (str) => (str ? md.render(str.toString()) : ''),
  },
];

class File {
  path = null;
  name = null;
  extension = null;
  language = null;
  _contents = null;

  constructor(path) {
    this.path = path;
    const filename = path.substring(path.lastIndexOf('/') + 1);
    const i = filename.lastIndexOf('.');
    if (i === -1) {
      this.name = filename;
    } else {
      this.name = filename.substring(0, i);
      this.extension = filename.substring(i + 1);

      if (this.name.indexOf('@') !== -1) {
        const extlang = this.name.split('@');
        this.name = extlang[0];
        this.language = extlang[1];
      }
    }
  }

  read = () => {
    return (this._contents ??= fs.readFileSync(this.path).toString());
  };
}

class Resource {
  dir;
  outputdir;
  queries = {};
  indexes = {};
  partials = {};
  hbs = helpers.reduce((context, { name, fn }) => {
    context.registerHelper(name, fn);
    return context;
  }, Handlebars.create());

  constructor(dir, outputdir) {
    this.dir = dir;
    this.outputdir = outputdir;
  }

  handleFile = (file) => {
    switch (file.extension) {
      case 'rq':
        this.queries[file.language] ??= {};
        this.queries[file.language][file.name] = decorateSPARQLResults(
          JSON.parse(file.read())
        );
        break;
      case 'handlebars':
      case 'hbs':
        if (file.name === 'index') {
          this.indexes[file.language] = file.read();
        } else {
          this.hbs.registerPartial(file.name, file.read());
        }
        break;
      default:
        console.warn(
          `Unknown file extension ${file.extension} for ${file.path}`
        );
    }
  };

  render = () => {
    Object.entries(this.indexes).forEach(([language, index]) => {
      console.log(
        `rendering language ${language} @ ${this.outputdir}/index@${language}.html`
      );
      fs.writeFileSync(
        `${this.outputdir}/index@${language}.html`,
        this.hbs.compile(index)(this.queries[language])
      );
    });
  };
}

// Initialize resource queue
const resources = [new Resource(INPUTDIR, OUTPUTDIR)];

// Iterate over resources
while (resources.length) {
  const resource = resources.pop();
  const contents = fs.readdirSync(resource.dir);

  // Scan directory
  contents.forEach((item) => {
    const path = subpath(resource.dir, item);
    const stat = fs.statSync(path);
    if (stat.isDirectory()) {
      // Capture resource for processing
      resources.push(new Resource(path, subpath(resource.outputdir, item)));
    } else if (stat.isFile()) {
      resource.handleFile(new File(path));
    } else {
      console.warn(`Unknown file type for ${path}`);
    }
  });

  // Resource rendering
  resource.render();
}
