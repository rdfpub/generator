package pub.rdf.builder;

import pub.rdf.config.RDFPUBConfig;
import pub.rdf.resource.RDFPUBResource;
import pub.rdf.resource.ResourceFile;
import pub.rdf.util.Program;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class NginxConfigBuilder extends ConfigurableBuilder {
    private final Path nginxdir;
    private final Path locationsconf;
    private final Path connegconf;
    private final Pattern compressed;
    private final Map<String,String> types = new HashMap<>(256);
    private final StringBuilder rconf = new StringBuilder(4096);
    private final StringBuilder lconf = new StringBuilder(4096);
    boolean configuredSPARQLEndpoint = false;

    private void copyConfigToOutput(final String filename) throws IOException {
        Files.writeString(nginxdir.resolve(filename),Program.readResource(filename));
    }

    public NginxConfigBuilder(final RDFPUBConfig config) {
        super(config);

        // Initialize file/directory locations
        nginxdir = config.getOutputDirectory().resolve("nginx");
        locationsconf = nginxdir.resolve("locations.nginx.conf");
        connegconf = nginxdir.resolve("conneg.nginx.conf");

        // Define regex pattern for compressed files
        compressed = Pattern.compile(config.getCompressFiles().stream().reduce("",(pattern,regex) -> pattern + "|(?:" + regex + ")").substring(1));

        // Define known/common MIME types
        Program.readResource("known-mime-types.csv").lines().forEach(line -> {
            final String[] kv = line.split(",");
            types.put(kv[0],kv[1]);
        });

        // Add user-defined MIME types
        types.putAll(config.getFileTypes());
    }

    @Override
    public void init() throws Exception {
        // Create/write special files and directories
        Files.createDirectory(nginxdir);
        Files.writeString(nginxdir.resolve("nginx.conf"),Program.readResource("nginx.conf").replace("$DEFAULT_LANG",config.getDefaultLanguage()).replace("$SERVER_NAME",config.getBaseURI().getHost()));
        Files.createFile(locationsconf);
        Files.writeString(connegconf,Program.readResource(connegconf.getFileName().toString()));

        // Copy static configs verbatim
        copyConfigToOutput("data-only.nginx.conf");
        copyConfigToOutput("index-only.nginx.conf");
        copyConfigToOutput("lang.nginx.conf");
        copyConfigToOutput("resource.nginx.conf");
        copyConfigToOutput("static.nginx.conf");
        copyConfigToOutput("static-compressed.nginx.conf");
        copyConfigToOutput("sparql-endpoint.nginx.conf");
    }

    @Override
    public void complete() throws IOException {
        // Cap off content negotiation config
        Files.writeString(connegconf,"}",StandardOpenOption.APPEND);

        // Output SPARQL endpoint configuration if not already configured
        if(!this.configuredSPARQLEndpoint) {
            Program.out("Appending SPARQL endpoint Nginx config");
            rconf.setLength(0);
            String sparqlPath = URI.create(config.getSPARQLEndpoint().toString()).getPath();
            final String slash = sparqlPath.charAt(sparqlPath.length() - 1) == '/' ? "" : "/";
            rconf
                .append("location = ")
                .append(sparqlPath)
                .append(" { include sparql-endpoint.nginx.conf; include data-only.nginx.conf; }\n")

                .append("location = ")
                .append(sparqlPath)
                .append(slash)
                .append("data.jsonld { add_header Content-Type \"application/ld+json\"; include static-compressed.nginx.conf; }\n")

                .append("location = ")
                .append(sparqlPath)
                .append(slash)
                .append("data.nt { add_header Content-Type \"text/ntriples\"; include static-compressed.nginx.conf; }\n")

                .append("location = ")
                .append(sparqlPath)
                .append(slash)
                .append("data.rdf { include add_header Content-Type \"application/rdf+xml\"; static-compressed.nginx.conf; }\n")

                .append("location = ")
                .append(sparqlPath)
                .append(slash)
                .append("data.ttl { include add_header Content-Type \"text/turtle\"; static-compressed.nginx.conf; }\n")
            ;

            try {
                Files.writeString(locationsconf,rconf.toString(),StandardOpenOption.APPEND);
            } catch (final IOException e) {
                throw new IOException(String.format("Failed to append Nginx config for %s",config.getSPARQLEndpoint()),e);
            }
        }
    }

    @Override
    public void always() {
        // noop
    }

    @Override
    public Exception handleNewResource(final RDFPUBResource resource) {
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
            case "handlebars":
            case "hbs":
                // reserved for other builders
                return null;
            default:
                try {
                    final boolean isCompressed = compressed.matcher(file.getPath().toString()).find();
                    if(isCompressed) {
                        Program.out("Enabling compression for file %s",file);
                    }
                    rconf.setLength(0);
                    rconf
                        .append("location = ")
                        .append(resource.isRootResource() ? "" : resource.getURIPath())
                        .append("/")
                        .append(file.getFileName())
                        .append(" { add_header Content-Type \"")
                        .append(types.getOrDefault(file.getExtension(),"text/plain"))
                        .append("\"; include static")
                        .append(isCompressed ? "-compressed" : "")
                        .append(".nginx.conf; }\n")
                    ;
                    Files.writeString(locationsconf,rconf.toString(),StandardOpenOption.APPEND);
                } catch (final IOException e) {
                    return new IOException(String.format("Failed to append Nginx config for %s",resource),e);
                } catch (final Exception e) {
                    return new Exception(String.format("Exception while attempting to write Nginx config for %s",resource));
                }
                return null;
        }
    }

    @Override
    public Exception handleFinishedResource(final RDFPUBResource resource) {

        // Reset StringBuilders
        rconf.setLength(0);
        lconf.setLength(0);

        // Determine root slash
        final String slash = resource.isRootResource() ? "" : "/";

        // SPARQL endpoint is special
        final boolean isSPARQLEndpoint = resource.equals(config.getSPARQLEndpoint());
        if(isSPARQLEndpoint) {
            Program.out("Processing SPARQL endpoint Nginx config");
            this.configuredSPARQLEndpoint = true;
        }

        // Handle resource data locations
        final boolean hasData = resource.hasData() || isSPARQLEndpoint;
        if(hasData) {
            // Append static RDF representations
            rconf
                .append("location = ")
                .append(resource.getURIPath())
                .append(slash)
                .append("data.jsonld { add_header Content-Type \"application/ld+json\"; include static-compressed.nginx.conf; }\n")

                .append("location = ")
                .append(resource.getURIPath())
                .append(slash)
                .append("data.nt { add_header Content-Type \"text/ntriples\"; include static-compressed.nginx.conf; }\n")

                .append("location = ")
                .append(resource.getURIPath())
                .append(slash)
                .append("data.rdf { add_header Content-Type \"application/rdf+xml\"; include static-compressed.nginx.conf; }\n")

                .append("location = ")
                .append(resource.getURIPath())
                .append(slash)
                .append("data.ttl { add_header Content-Type \"text/turtle\"; include static-compressed.nginx.conf; }\n")
            ;
        }

        // Handle resource index templates
        final boolean hasIndexTemplates = !resource.getIndexTemplates().isEmpty();
        if(hasIndexTemplates) {
            // Append resource language config
            lconf
                .append("  conneg.accept_language.register(\"languages")
                .append(resource.getURIPath())
                .append("\",\"")
            ;

            // Append resource HTML representations and language redirects
            for(final String language : resource.getLanguages()) {
                rconf
                    .append("location = ")
                    .append(resource.getURIPath())
                    .append(slash)
                    .append("index@")
                    .append(language)
                    .append(".html { add_header Content-Type \"text/html\"; include static-compressed.nginx.conf; }\n")
                    .append("location = ")
                    .append(resource.getURIPath())
                    .append('@')
                    .append(language)
                    .append(" { set $lang \"")
                    .append(language)
                    .append("\"; set $target \"")
                    .append(resource.getURIPath())
                    .append("\"; include lang.nginx.conf; }\n")
                ;

                // Append resource languages
                lconf
                    .append(language)
                    .append(",")
                ;
            }

            // Terminate languages config
            lconf
                .deleteCharAt(lconf.length() - 1)
                .append("\")\n")
            ;
        }

        // Write main resource type config if resource has any content
        if(hasIndexTemplates || hasData) {
            rconf
                .append("location = ")
                .append(resource.getURIPath())
                .append(" { include ")
                .append(isSPARQLEndpoint ? "sparql-endpoint.nginx.conf; include " : "")
                .append(hasIndexTemplates && hasData ? "resource" : hasIndexTemplates ? "index-only" : "data-only")
                .append(".nginx.conf; }\n")
            ;
        } else {
            return null; // don't write anything for 'blank' resources
        }

        try {
            // Write resource location config
            Files.writeString(locationsconf,rconf.append("\n").toString(),StandardOpenOption.APPEND);

            // Write language config if the resource has index templates
            if(hasIndexTemplates) {
                Files.writeString(connegconf, lconf.toString(), StandardOpenOption.APPEND);
            }
        } catch (final IOException e) {
            return new IOException(String.format("Failed to append Nginx config for %s",resource),e);
        } catch (final Exception e) {
            return new Exception(String.format("Exception while attempting to write Nginx config for %s",resource));
        }
        return null;
    }
}
