package pub.rdf.resource;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.TupleQuery;
import pub.rdf.config.RDFPUBConfig;

import java.net.URI;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class RDFPUBResource implements IRI {
    private final IRI iri;
    private final URI uri;
    private final Map<String,String> namespaces = new HashMap<>(8);
    private final Path resourcePath;
    private final Path layoutPath;
    private boolean hasData = false;
    private final Map<String, TupleQuery> queries = new HashMap<>(16);

    private final Set<ResourceFile> indexTemplates = new HashSet<>(8);
    private final Map<String,ResourceFile> partialTemplates = new HashMap<>(8);

    public RDFPUBResource(final RDFPUBConfig config, final Path path) {
        this.resourcePath = config.getOutputDirectory().resolve("resources").resolve(path);
        this.layoutPath = config.getOutputDirectory().resolve("layouts").resolve(path);
        this.uri = config.getBaseURI().resolve(path.toString());
        this.iri = SimpleValueFactory.getInstance().createIRI(this.uri.toString());
    }

    public String getURIPath() {
        return this.uri.getPath();
    }

    public boolean isRootResource() {
        return this.getURIPath().length() == 1;
    }

    @Override
    public boolean isBNode() {
        return false;
    }

    @Override
    public boolean isIRI() {
        return true;
    }

    @Override
    public boolean isLiteral() {
        return false;
    }

    @Override
    public boolean isTriple() {
        return false;
    }

    @Override
    public String getNamespace() {
        return this.iri.getNamespace();
    }

    @Override
    public String getLocalName() {
        return this.iri.getLocalName();
    }

    @Override
    public String stringValue() {
        return this.iri.stringValue();
    }

    @Override
    public String toString() {
        return this.iri.stringValue();
    }

    public Map<String,String> getPrefixes() {
        return this.namespaces;
    }

    public Map<String,TupleQuery> getQueries() {
        return queries;
    }

    public Set<String> getLanguages() { return indexTemplates.stream().map(ResourceFile::getLanguage).collect(Collectors.toSet()); }

    public Path getResourcePath() {
        return resourcePath;
    }

    public Path getLayoutPath() {
        return layoutPath;
    }

    public boolean hasLayout() {
        return !this.partialTemplates.isEmpty() || !this.queries.isEmpty();
    }

    @Override
    public boolean equals(final Object o) {
        if(o instanceof final RDFPUBResource r) {
            return r.iri.equals(iri);
        } else if(o instanceof final IRI i){
            return this.iri.equals(i);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.iri.hashCode();
    }

    public Map<String,ResourceFile> getPartialTemplates() {
        return partialTemplates;
    }

    public Set<ResourceFile> getIndexTemplates() { return indexTemplates; }

    public boolean hasData() {
        return hasData;
    }

    public void hasData(final boolean does) {
        hasData = does;
    }
}
