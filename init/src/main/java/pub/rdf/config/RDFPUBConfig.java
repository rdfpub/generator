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
        public List<String> CompressFiles = Collections.emptyList();
        public Map<String,String> FileTypes = Collections.emptyMap();
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
    private final List<String> CompressFiles;
    private final Map<String,String> FileTypes;

    private RDFPUBConfig(
        final Path inputdir,
        final Path outpudir,
        final URI BaseURI,
        final String DefaultLanguage,
        final boolean CleanOutputDirectory,
        final IRI SPARQLEndpoint,
        final Map<String,String> Prefixes,
        final Map<String,String> FileTypes,
        final List<String> IncludeFiles,
        final List<String> ExcludeFiles,
        final List<String> CompressFiles
    ) {
        this.InputDirectory = inputdir;
        this.OutputDirectory = outpudir;
        this.BaseURI = BaseURI;
        this.DefaultLanguage = DefaultLanguage;
        this.CleanOutputDirectory = CleanOutputDirectory;
        this.SPARQLEndpoint = SPARQLEndpoint;
        this.Prefixes = Prefixes;
        this.FileTypes = FileTypes;
        this.IncludeFiles = IncludeFiles;
        this.ExcludeFiles = ExcludeFiles;
        this.CompressFiles = CompressFiles;
    }

    // Function for shrinking a list down to a compact ArrayList
    private static List<String> shrinkList(final List<String> list) {
        final List<String> tempList = new ArrayList<>(list.size());
        tempList.addAll(list);
        return tempList;
    }

    public static RDFPUBConfig load(final Path inputdir, final Path outputdir)throws IOException {
        // Load YAML file
        final String configstr = Files.readString(inputdir.resolve(".rdfpub"));
        final Yaml yaml = new Yaml(new Constructor(RDFPUBConfigInternal.class));
        final RDFPUBConfigInternal configInternal = yaml.load(configstr);

        // Get base URI
        final URI base = URI.create(configInternal.BaseURI);

        // Compact and resolve URI prefixes
        Map<String,String> tempPrefixes = configInternal.FileTypes;
        if(!configInternal.Prefixes.isEmpty()) {
            tempPrefixes = new HashMap<>((int) (configInternal.Prefixes.size() / 0.75 + 1));
            tempPrefixes.putAll(configInternal.Prefixes.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, prefix -> base.resolve(prefix.getValue()).toString())));
        }

        // Compact custom file type definitions
        Map<String,String> tempFileTypes = configInternal.FileTypes;
        if(!tempFileTypes.isEmpty()) {
            tempFileTypes = new HashMap<>((int) (configInternal.FileTypes.size() / 0.75 + 1));
            tempFileTypes.putAll(configInternal.FileTypes);
        }

        // Return final config
        return new RDFPUBConfig(
            inputdir,
            outputdir,
            base.resolve("/"),
            configInternal.DefaultLanguage.toLowerCase(Locale.ROOT),
            configInternal.CleanOutputDirectory,
            SimpleValueFactory.getInstance().createIRI(base.resolve(configInternal.SPARQLEndpoint).toString()),
            tempPrefixes,
            tempFileTypes,
            shrinkList(configInternal.IncludeFiles),
            shrinkList(configInternal.ExcludeFiles),
            shrinkList(configInternal.CompressFiles)
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

    public List<String> getCompressFiles() { return CompressFiles; }

    public Map<String,String> getFileTypes() { return FileTypes; }

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
