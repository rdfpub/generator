package pub.rdf.util;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public enum VOID implements IRI {
    TRIPLES("http://rdfs.org/ns/void#triples");

    private final IRI iri;
    VOID(final String iri) {
        this.iri = SimpleValueFactory.getInstance().createIRI(iri);
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
        return this.stringValue();
    }
}
