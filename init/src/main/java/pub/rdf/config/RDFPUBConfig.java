package pub.rdf.config;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public final class RDFPUBConfig {
    private final static class RDFPUBConfigInternal {
        public String BaseURI = null;
        public String DefaultLanguage = null;
        public boolean CleanOutputDirectory = false;
        public String SPARQLEndpoint = "/sparql";
        public Map<String,String> Prefixes = Collections.emptyMap();
        public List<String> ExcludeFiles = Collections.emptyList();
        public List<String> IncludeFiles = Collections.emptyList();
    }

    private final Path InputDirectory;
    private final Path OutputDirectory;
    private final URI BaseURI;
    private final String DefaultLanguage;
    private final boolean CleanOutputDirectory;
    private final IRI SPARQLEndpoint;
    private final Map<String,String> Prefixes;
    private final List<String> ExcludeFiles;
    private final List<String> IncludeFiles;

    private RDFPUBConfig(
        final Path inputdir,
        final Path outpudir,
        final URI BaseURI,
        final String DefaultLanguage,
        final boolean CleanOutputDirectory,
        final IRI SPARQLEndpoint,
        final Map<String,String> Prefixes,
        final List<String> IncludeFiles,
        final List<String> ExcludeFiles
    ) {
        this.InputDirectory = inputdir;
        this.OutputDirectory = outpudir;
        this.BaseURI = BaseURI;
        this.DefaultLanguage = DefaultLanguage;
        this.CleanOutputDirectory = CleanOutputDirectory;
        this.SPARQLEndpoint = SPARQLEndpoint;
        this.Prefixes = Prefixes;
        this.IncludeFiles = IncludeFiles;
        this.ExcludeFiles = ExcludeFiles;
    }

    public static RDFPUBConfig load(final Path inputdir, final Path outputdir)throws IOException {
        // Load YAML file
        final String configstr = Files.readString(inputdir.resolve(".rdfpub"));
        final Yaml yaml = new Yaml(new Constructor(RDFPUBConfigInternal.class));
        final RDFPUBConfigInternal configInternal = yaml.load(configstr);

        // Get base URI
        final URI base = URI.create(configInternal.BaseURI);

        // Copy collections to more efficiently sized collections
        final Map<String,String> tempPrefixes = new HashMap<>((int) (configInternal.Prefixes.size() / 0.75 + 1));
        tempPrefixes.putAll(configInternal.Prefixes.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, prefix -> base.resolve(prefix.getValue()).toString())));
        final List<String> tempExcludeFiles = new ArrayList<>(configInternal.ExcludeFiles.size());
        tempExcludeFiles.addAll(configInternal.ExcludeFiles);
        final List<String> tempIncludeFiles = new ArrayList<>(configInternal.IncludeFiles.size());
        tempIncludeFiles.addAll(configInternal.IncludeFiles);

        // Return final config
        return new RDFPUBConfig(
            inputdir,
            outputdir,
            base.resolve("/"),
            configInternal.DefaultLanguage.toLowerCase(Locale.ROOT),
            configInternal.CleanOutputDirectory,
            SimpleValueFactory.getInstance().createIRI(base.resolve(configInternal.SPARQLEndpoint).toString()),
            tempPrefixes,
            tempIncludeFiles,
            tempExcludeFiles
        );
    }

    public Path getInputDirectory() {
        return InputDirectory;
    }

    public Path getOutputDirectory() {
        return OutputDirectory;
    }

    public URI getBaseURI() {
        return BaseURI;
    }

    public String getDefaultLanguage() {
        return DefaultLanguage;
    }

    public IRI getSPARQLEndpoint() { return SPARQLEndpoint; }

    public Map<String, String> getPrefixes() {
        return Prefixes;
    }

    public List<String> getExcludeFiles() {
        return ExcludeFiles;
    }

    public List<String> getIncludeFiles() {
        return IncludeFiles;
    }

    public boolean cleanOutputDirectory() {
        return CleanOutputDirectory;
    }
}
