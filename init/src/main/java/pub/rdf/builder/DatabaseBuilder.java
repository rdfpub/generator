package pub.rdf.builder;

import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.parser.QueryPrologLexer;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.RDFParser;
import org.eclipse.rdf4j.rio.helpers.AbstractRDFHandler;
import org.eclipse.rdf4j.rio.helpers.BasicParserSettings;
import org.eclipse.rdf4j.rio.helpers.BasicWriterSettings;
import org.eclipse.rdf4j.rio.jsonld.JSONLDParser;
import org.eclipse.rdf4j.rio.jsonld.JSONLDWriter;
import org.eclipse.rdf4j.rio.ntriples.NTriplesParser;
import org.eclipse.rdf4j.rio.ntriples.NTriplesWriter;
import org.eclipse.rdf4j.rio.rdfxml.RDFXMLParser;
import org.eclipse.rdf4j.rio.rdfxml.RDFXMLWriter;
import org.eclipse.rdf4j.rio.turtle.TurtleParser;
import org.eclipse.rdf4j.rio.turtle.TurtleWriter;
import org.eclipse.rdf4j.sail.lmdb.LmdbStore;
import org.eclipse.rdf4j.sail.lmdb.config.LmdbStoreConfig;
import org.eclipse.rdf4j.sail.nativerdf.NativeStore;
import pub.rdf.config.RDFPUBConfig;
import pub.rdf.resource.ResourceFile;
import pub.rdf.util.DCTerms;
import pub.rdf.resource.RDFPUBResource;
import pub.rdf.util.Program;
import pub.rdf.util.SD;
import pub.rdf.util.VOID;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static pub.rdf.util.Program.out;

public class DatabaseBuilder extends ConfigurableBuilder {
    public final static BNode SDROOT = SimpleValueFactory.getInstance().createBNode("sdroot");
    public final static BNode DATASET = SimpleValueFactory.getInstance().createBNode("dataset");

    private final Path dbdir;
    private RepositoryConnection connection = null;
    private final Set<Namespace> namespaces = new HashSet<>(16);

    public DatabaseBuilder(final RDFPUBConfig config) {
        super(config);
        this.dbdir = config.getOutputDirectory().resolve("db");

        // Collect prefixes as RDF4J namespaces
        namespaces.addAll(config.getPrefixes().entrySet().stream().map(prefix -> new SimpleNamespace(prefix.getKey(),prefix.getValue())).collect(Collectors.toSet()));
    }

    @Override
    public void init() throws IOException {
        // Create repository
        Files.createDirectory(dbdir);
        final Repository database = new SailRepository(new NativeStore(dbdir.toFile(), "cspo,spoc,posc"));
        database.init();
        connection = database.getConnection();

        // Initial data
        connection.begin();

        // Define namespaces
        connection.clearNamespaces();
        for(final Map.Entry<String,String> namespace : config.getPrefixes().entrySet()) {
            connection.setNamespace(namespace.getKey(),namespace.getValue());
        }

        // SPARQL endpoint service description
        connection.add(SDROOT, RDF.TYPE, SD.SERVICE, config.getSPARQLEndpoint());
        connection.add(SDROOT, SD.ENDPOINT, config.getSPARQLEndpoint(), config.getSPARQLEndpoint());
        connection.add(SDROOT, SD.SUPPORTEDLANGUAGE, SD.SPARQL11QUERY, config.getSPARQLEndpoint());
        connection.add(SDROOT, SD.DEFAULTDATASET, DATASET, config.getSPARQLEndpoint());

        // Commit changes
        connection.commit();
    }

    @Override
    public void complete() throws IOException {
        // Finalize the SPARQL service description
        final ValueFactory rdf = SimpleValueFactory.getInstance();
        final BNode dn = rdf.createBNode();
        connection.begin();

        // Add SPARQL endpoint metadata
        final BNode rn = rdf.createBNode();
        final BNode gn = rdf.createBNode();
        connection.add(rdf.createStatement(DATASET, SD.NAMEDGRAPH, rn, config.getSPARQLEndpoint()));
        connection.add(rdf.createStatement(rn, RDF.TYPE, SD.NAMEDGRAPHCLASS, config.getSPARQLEndpoint()));
        connection.add(rdf.createStatement(rn, SD.NAME, config.getSPARQLEndpoint(), config.getSPARQLEndpoint()));
        connection.add(rdf.createStatement(rn, SD.GRAPH, gn, config.getSPARQLEndpoint()));
        connection.add(rdf.createStatement(gn, RDF.TYPE, SD.GRAPHCLASS, config.getSPARQLEndpoint()));
        connection.add(rdf.createStatement(gn, VOID.TRIPLES, rdf.createLiteral(connection.size(config.getSPARQLEndpoint())), config.getSPARQLEndpoint()));
        connection.add(rdf.createStatement(rn, DCTerms.CREATED, rdf.createLiteral(new Date()), config.getSPARQLEndpoint()));

        connection.add(DATASET, SD.DEFAULTDATASET, dn, config.getSPARQLEndpoint());
        connection.add(dn, RDF.TYPE, SD.GRAPHCLASS, config.getSPARQLEndpoint());
        connection.add(dn, VOID.TRIPLES, SimpleValueFactory.getInstance().createLiteral(connection.size() + 1), config.getSPARQLEndpoint());
        connection.commit();

        // Write out data files for SPARQL endpoint
        final Path sparqldir = config.getOutputDirectory().resolve("resources").resolve("." + URI.create(config.getSPARQLEndpoint().toString()).getPath()).normalize();
        Files.createDirectories(sparqldir);
        final Exception e = writeRDF(config.getSPARQLEndpoint().toString(),config.getPrefixes());
        if(e != null) {
            throw new IOException("Unable to write RDF files for SPARQL endpoint",e);
        }

        // Write out the config for this database
        Files.writeString(dbdir.resolve("config.ttl"),Program.readResource("config.ttl"));
    }

    @Override
    public void always() {
        connection.close();
    }

    @Override
    public Exception handleNewResource(final RDFPUBResource resource) {
        // Set data flag for SPARQL endpoint resource which always has data
        if(resource.equals(config.getSPARQLEndpoint())) {
            resource.hasData(true);
        }
        return null;
    }

    @Override
    public Exception handleResourceFile(final RDFPUBResource resource, final ResourceFile file) {
        RDFParser parser = null;
        switch (file.getExtension()) {
            case "ttl":
                parser = new TurtleParser();
                break;
            case "rdf":
            case "rdfxml":
                parser = new RDFXMLParser();
                break;
            case "nt":
            case "ntriples":
                parser = new NTriplesParser();
                break;
            case "json":
            case "jsonld":
                parser = new JSONLDParser();
                break;
            case "sparql":
            case "rq":
                try {
                    // Find existing namespaces in query
                    final String query = Files.readString(file.getPath());
                    final Set<String> queryPrefixes = QueryPrologLexer
                        .lex(query)
                        .stream()
                        .filter(token -> token.getType() == QueryPrologLexer.TokenType.PREFIX)
                        .map(QueryPrologLexer.Token::getStringValue)
                        .collect(Collectors.toSet())
                    ;

                    // Create set of global prefixes with existing query prefixes removed
                    final String globalPrefixes = config
                        .getPrefixes()
                        .entrySet()
                        .stream()
                        .filter(prefix -> !queryPrefixes.contains(prefix.getKey()))
                        .reduce("",(str, prefix) -> str + "PREFIX " + prefix.getKey() + ": <" + prefix.getValue() + ">\n",(a,b) -> a + b)
                    ;

                    // Prepare query with prepended with undefined prefixes
                    resource.getQueries().putIfAbsent(file.getName(), connection.prepareTupleQuery(QueryLanguage.SPARQL, globalPrefixes + query, config.getBaseURI().toString()));
                } catch (final MalformedQueryException e) {
                    return new MalformedQueryException(String.format("%s is not a valid SPARQL tuple query, ignoring", file));
                } catch (final IOException e) {
                    return new IOException(String.format("Failed to read %s during database query preparation", file), e);
                }
                return null;
        }

        // Parse RDF
        if(parser != null) {
            // Check for SPARQL endpoint which is special
            if(resource.equals(config.getSPARQLEndpoint())) {
                Program.err("WARNING: RDF file %s found for SPARQL endpoint URL <%s>; ignoring",file,config.getSPARQLEndpoint());
                return null;
            }

            // Set handler
            parser.setRDFHandler(new AbstractRDFHandler() {
                int triples = 0;
                @Override
                public void handleNamespace(final String name, final String prefix) {
                    resource.getPrefixes().put(name, prefix);
                }

                @Override
                public void handleStatement(final Statement st) throws RDFHandlerException {
                    connection.add(st,resource);
                    triples++;
                }

                @Override
                public void endRDF() {
                    out(String.format("Added %s RDF triples to %s", triples, resource));
                }
            });

            // Initialize namespaces
            parser.getParserConfig().set(BasicParserSettings.NAMESPACES,namespaces);

            // Commit data
            try {
                connection.begin();
                parser.parse(new FileInputStream(file.getPath().toFile()), resource.toString());
                connection.commit();
                resource.hasData(true);
            } catch (final RDFParseException e) {
                return new RDFParseException("File " + file.getPath().toString() + " is not valid syntax for its file extension.",e);
            } catch (final IOException e) {
                return new IOException(String.format("Error reading file %s during RDF parsing",file),e);
            }
        }
        return null;
    }

    @Override
    public Exception handleFinishedResource(final RDFPUBResource resource) {
        // Generate static resource RDF from database
        if (resource.hasData()) {
            // Initialize local variables
            final ValueFactory rdf = SimpleValueFactory.getInstance();

            // Add resource metadata
            final BNode rn = rdf.createBNode();
            final BNode gn = rdf.createBNode();
            connection.begin();
            connection.add(rdf.createStatement(DATASET, SD.NAMEDGRAPH, rn, config.getSPARQLEndpoint()));
            connection.add(rdf.createStatement(rn, RDF.TYPE, SD.NAMEDGRAPHCLASS, config.getSPARQLEndpoint()));
            connection.add(rdf.createStatement(rn, SD.NAME, resource, config.getSPARQLEndpoint()));
            connection.add(rdf.createStatement(rn, SD.GRAPH, gn, config.getSPARQLEndpoint()));
            connection.add(rdf.createStatement(gn, RDF.TYPE, SD.GRAPHCLASS, config.getSPARQLEndpoint()));
            connection.add(rdf.createStatement(gn, VOID.TRIPLES, rdf.createLiteral(connection.size(resource)), config.getSPARQLEndpoint()));
            connection.add(rdf.createStatement(rn, DCTerms.CREATED, rdf.createLiteral(new Date()), config.getSPARQLEndpoint()));
            connection.commit();

            // Write out RDF files for resource
            return writeRDF(resource.stringValue(), Stream.concat(resource.getPrefixes().entrySet().stream(),config.getPrefixes().entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey,Map.Entry::getValue,(r,g) -> r)));
        }
        return null;
    }

    private Exception writeRDF(final String graph, final Map<String,String> prefixes) {
        final Path outputPath = config.getOutputDirectory().resolve("resources").resolve("." + URI.create(graph).getPath()).normalize();
        final NTriplesWriter nt;
        final TurtleWriter ttl;
        final RDFXMLWriter rdfxml;
        final JSONLDWriter jsonld;
        try {
            nt = new NTriplesWriter(new FileOutputStream(outputPath.resolve("data.nt").toFile()));
            ttl = new TurtleWriter(new FileOutputStream(outputPath.resolve("data.ttl").toFile()));
            rdfxml = new RDFXMLWriter(new FileOutputStream(outputPath.resolve("data.rdf").toFile()));
            jsonld = new JSONLDWriter(new FileOutputStream(outputPath.resolve("data.jsonld").toFile()));
        } catch (final FileNotFoundException ex) {
            return new Exception(String.format("Cannot output serialized resource RDF for <%s> to file", graph), ex);
        }
        connection.prepareGraphQuery("CONSTRUCT { $s $p $o } WHERE { GRAPH <" + graph + "> { $s $p $o } }").evaluate(new RDFHandler() {
            @Override
            public void startRDF() throws RDFHandlerException {
                // Initialize writers
                nt.startRDF();
                ttl.startRDF();
                rdfxml.startRDF();
                jsonld.startRDF();

                // Configure writers
                nt.getSupportedSettings().add(BasicWriterSettings.INLINE_BLANK_NODES);
                ttl.getSupportedSettings().add(BasicWriterSettings.INLINE_BLANK_NODES);
                rdfxml.getSupportedSettings().add(BasicWriterSettings.INLINE_BLANK_NODES);
                jsonld.getSupportedSettings().add(BasicWriterSettings.INLINE_BLANK_NODES);

                // Feed prefixes into RDF output
                prefixes.forEach(this::handleNamespace);
            }

            @Override
            public void endRDF() throws RDFHandlerException {
                nt.endRDF();
                ttl.endRDF();
                rdfxml.endRDF();
                jsonld.endRDF();
            }

            @Override
            public void handleNamespace(final String prefix, final String name) throws RDFHandlerException {
                ttl.handleNamespace(prefix, name);
                rdfxml.handleNamespace(prefix, name);
                jsonld.handleNamespace(prefix, name);
            }

            @Override
            public void handleStatement(final Statement st) throws RDFHandlerException {
                nt.handleStatement(st);
                ttl.handleStatement(st);
                rdfxml.handleStatement(st);
                jsonld.handleStatement(st);
            }

            @Override
            public void handleComment(final String comment) throws RDFHandlerException {
                // noop
            }
        });
        return null;
    }
}
