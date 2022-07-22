const fs = require('fs');
const url = require('url');
const Handlebars = require('handlebars');
const MarkdownIt = require('markdown-it');

// Get processing directories
const INPUTDIR = process.argv[2];
const OUTPUTDIR = process.argv[3];
const BASEURI = process.argv[4];

console.log(`Input directory is ${INPUTDIR}`);
console.log(`Output directory is ${OUTPUTDIR}`);
console.log(`Base URI is ${BASEURI}`);

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
    // Equals operator: {{# if (equals person.name friend.name) }}
    name: 'equals',
    fn: (...values) => {
      return values.slice(0, -1).every((v) => v === values[0]);
    },
  },
  {
    // Markdown: {{{markdown query.description}}}
    name: 'markdown',
    fn: (str) => (str ? md.render(str.toString()) : ''),
  },
  {
    // Relative URL's: <a href="{{relative person.url}}">{{person.name}}</a>
    name: 'relative',
    fn: (str) => {
      const link = new URL(str, BASEURI);
      return link.pathname + link.search + link.hash;
    },
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
  url;

  constructor(dir, outputdir, url) {
    this.dir = dir;
    this.outputdir = outputdir;
    this.url = url;
  }

  handleFile = (file) => {
    switch (file.extension) {
      case 'rq':
        this.queries[file.language] ??= {
          $resource: this.url.href,
          $language: file.language,
        };
        this.queries[file.language][file.name] = decorateSPARQLResults(
          JSON.parse(file.read())
        );
        break;
      case 'md':
      case 'html':
        if (file.name === 'index') {
          const template = this.hbs.compile(file.read());
          this.indexes[file.language] = file.extension === 'md' ? data => md.render(template(data)) : template;
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
        index(this.queries[language])
      );
    });
  };
}

// Initialize resource queue
const resources = [new Resource(INPUTDIR, OUTPUTDIR, new URL('/', BASEURI))];

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
      resources.push(
        new Resource(
          path,
          subpath(resource.outputdir, item),
          new URL(
            resource.url.href +
              (resource.url.pathname === '/' ? '' : '/') +
              item
          )
        )
      );
    } else if (stat.isFile()) {
      resource.handleFile(new File(path));
    } else {
      console.warn(`Unknown file type for ${path}`);
    }
  });

  // Resource rendering
  resource.render();
}
