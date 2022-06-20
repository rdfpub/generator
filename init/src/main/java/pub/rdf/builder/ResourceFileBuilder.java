package pub.rdf.builder;

import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.resultio.sparqljson.SPARQLResultsJSONWriter;
import pub.rdf.config.RDFPUBConfig;
import pub.rdf.resource.RDFPUBResource;
import pub.rdf.resource.ResourceFile;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.util.LinkedList;
import java.util.Map;

import static pub.rdf.util.Program.out;

public class ResourceFileBuilder extends ConfigurableBuilder {
    private final Path resourcedir;
    private final Path layoutdir;
    private final LinkedList<RDFPUBResource> layouts = new LinkedList<>();

    public ResourceFileBuilder(final RDFPUBConfig config) {
        super(config);
        this.resourcedir = config.getOutputDirectory().resolve("resources");
        this.layoutdir = config.getOutputDirectory().resolve("layouts");
    }

    @Override
    public void init() throws IOException {
        Files.createDirectory(resourcedir);
        Files.createDirectory(layoutdir);
    }

    @Override
    public void complete() throws IOException {
        // Do template processing
        for(final RDFPUBResource resource : layouts) {
            if(resource.getIndexTemplates().isEmpty()) {
                out(String.format("No index templates for %s",resource));
                continue;
            }

            out(String.format("Processing layout for %s",resource));
            for(final ResourceFile index : resource.getIndexTemplates()) {
                // Copy file to output location
                Files.copy(index.getPath(),resource.getLayoutPath().resolve(index.getFileName()));

                // Execute queries for this index file
                final String language = index.getLanguage();
                for (final Map.Entry<String, TupleQuery> query : resource.getQueries().entrySet()) {
                    out(String.format("Executing query %s", query.getKey()));
                    query.getValue().setBinding("resource", resource);
                    query.getValue().setBinding("language", SimpleValueFactory.getInstance().createLiteral(language));
                    query.getValue().evaluate(new SPARQLResultsJSONWriter(new FileOutputStream(resource.getLayoutPath().resolve(String.format("%s@%s.rq", query.getKey(),language)).toFile())));
                }
            }

            for(final Map.Entry<String, ResourceFile> partialTemplate : resource.getPartialTemplates().entrySet()) {
                out(String.format("Copying partial template %s",partialTemplate.getKey()));
                Files.copy(partialTemplate.getValue().getPath(),resource.getLayoutPath().resolve(String.format("%s.handlebars",partialTemplate.getKey())));
            }
        }
    }

    @Override
    public void always() {
        // noop
    }

    @Override
    public Exception handleNewResource(final RDFPUBResource resource) {
        // Create new resource output directory that mirrors input directory
        try {
            Files.createDirectory(resource.getResourcePath());
        } catch (final FileAlreadyExistsException e) {
            // ignore this
        } catch (final IOException e) {
            return new IOException(String.format("Error while creating resource directory for <%s>",resource),e);
        }

        // Create new layout output directory that mirrors input directory
        try {
            Files.createDirectory(resource.getLayoutPath());
        } catch (final FileAlreadyExistsException e) {
            // ignore this
        } catch (final IOException e) {
            return new IOException(String.format("Error while creating layout directory for <%s>",resource),e);
        }

        // Capture resource for layout handling
        layouts.offerFirst(resource);

        return null;
    }

    @Override
    public Exception handleResourceFile(final RDFPUBResource resource, final ResourceFile file) {
        switch(file.getExtension()) {
            case "ttl":
            case "rdf":
            case "rdfxml":
            case "nt":
            case "ntriples":
            case "json":
            case "jsonld":
            case "rq":
            case "sparql":
                // reserved for other builders
                return null;
            case "handlebars":
            case "hbs":
                if(file.isIndexTemplate()) {
                    if(file.getLanguage() == null) {
                        file.setLanguage(config.getDefaultLanguage());
                    }
                    resource.getIndexTemplates().add(file);
                } else {
                    resource.getPartialTemplates().putIfAbsent(file.getName(),file);
                }
                return null;
            default:
                out(String.format("Processing %s as static file", file));
                final Path relative = config.getInputDirectory().relativize(file.getPath());
                try {
                    Files.copy(file.getPath(), resourcedir.resolve(relative));
                } catch (final IOException e) {
                    return new IOException(String.format("Failed to copy %s from input directory to output directory", file), e);
                }
                return null;
        }
    }

    @Override
    public Exception handleFinishedResource(final RDFPUBResource resource) {
        // Propagate layout files
        if(resource.hasLayout()) {
            out("Propagating layout for %s", resource);
            for (final RDFPUBResource child : layouts) {
                if (child.equals(resource)) {
                    break;
                } else {
                    out(String.format("Propagating to child %s", child));

                    // Propagate queries
                    for (final Map.Entry<String, TupleQuery> query : resource.getQueries().entrySet()) {
                        child.getQueries().putIfAbsent(query.getKey(), query.getValue());
                    }

                    // Propagate partial templates
                    for (final Map.Entry<String, ResourceFile> partialTemplate : resource.getPartialTemplates().entrySet()) {
                        child.getPartialTemplates().putIfAbsent(partialTemplate.getKey(), partialTemplate.getValue());
                    }
                }
            }
        } else {
            out("No layout to propagate for %s",resource);
        }

        // Check for a default language index if the resource has index templates
        boolean rendersDefaultLanguage = resource.getIndexTemplates().isEmpty();
        for(final ResourceFile file : resource.getIndexTemplates()) {
            if(config.getDefaultLanguage().equals(file.getLanguage())) {
                rendersDefaultLanguage = true;
                break;
            }
        }

        if(!rendersDefaultLanguage) {
            return new Exception(String.format("Resource <%s> has no index template for the default language '%s'",resource,config.getDefaultLanguage()));
        } else {
            return null;
        }
    }
}
