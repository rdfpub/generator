package pub.rdf.util;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public enum SD implements IRI {
    ENDPOINT("http://www.w3.org/ns/sparql-service-description#endpoint"),
    FEATURE("http://www.w3.org/ns/sparql-service-description#feature"),
    DEFAULTENTAILMENTREGIME("http://www.w3.org/ns/sparql-service-description#defaultEntailmentRegime"),
    ENTAILMENTREGIME("http://www.w3.org/ns/sparql-service-description#entailmentRegime"),
    DEFAULTSUPPORTEDENTAILMENTPROFILE("http://www.w3.org/ns/sparql-service-description#defaultSupportedEntailmentProfile"),
    SUPPORTEDENTAILMENTPROFILE("http://www.w3.org/ns/sparql-service-description#supportedEntailmentProfile"),
    EXTENSIONFUNCTION("http://www.w3.org/ns/sparql-service-description#extensionFunction"),
    EXTENSIONAGGREGATE("http://www.w3.org/ns/sparql-service-description#extensionAggregate"),
    LANGUAGEEXTENSION("http://www.w3.org/ns/sparql-service-description#languageExtension"),
    SUPPORTEDLANGUAGE("http://www.w3.org/ns/sparql-service-description#supportedLanguage"),
    PROPERTYFEATURE("http://www.w3.org/ns/sparql-service-description#propertyFeature"),
    DEFAULTDATASET("http://www.w3.org/ns/sparql-service-description#defaultDataset"),
    AVAILABLEGRAPHS("http://www.w3.org/ns/sparql-service-description#availableGraphs"),
    RESULTFORMAT("http://www.w3.org/ns/sparql-service-description#resultFormat"),
    INPUTFORMAT("http://www.w3.org/ns/sparql-service-description#inputFormat"),
    DEFAULTGRAPH("http://www.w3.org/ns/sparql-service-description#defaultGraph"),
    NAMEDGRAPH("http://www.w3.org/ns/sparql-service-description#namedGraph"),
    NAME("http://www.w3.org/ns/sparql-service-description#name"),
    GRAPH("http://www.w3.org/ns/sparql-service-description#graph"),
    SERVICE("http://www.w3.org/ns/sparql-service-description#Service"),
    FEATURECLASS("http://www.w3.org/ns/sparql-service-description#Feature"),
    LANGUAGE("http://www.w3.org/ns/sparql-service-description#Language"),
    FUNCTION("http://www.w3.org/ns/sparql-service-description#Function"),
    AGGREGATE("http://www.w3.org/ns/sparql-service-description#Aggregate"),
    ENTAILMENTREGIMECLASS("http://www.w3.org/ns/sparql-service-description#EntailmentRegime"),
    ENTAILMENTPROFILE("http://www.w3.org/ns/sparql-service-description#EntailmentProfile"),
    GRAPHCOLLECTION("http://www.w3.org/ns/sparql-service-description#GraphCollection"),
    DATASET("http://www.w3.org/ns/sparql-service-description#Dataset"),
    GRAPHCLASS("http://www.w3.org/ns/sparql-service-description#Graph"),
    NAMEDGRAPHCLASS("http://www.w3.org/ns/sparql-service-description#NamedGraph"),
    SPARQL10QUERY("http://www.w3.org/ns/sparql-service-description#SPARQL10Query"),
    SPARQL11QUERY("http://www.w3.org/ns/sparql-service-description#SPARQL11Query"),
    SPARQL11UPDATE("http://www.w3.org/ns/sparql-service-description#SPARQL11Update"),
    DEREFERENCESURIS("http://www.w3.org/ns/sparql-service-description#DereferencesURIs"),
    UNIONDEFAULTGRAPH("http://www.w3.org/ns/sparql-service-description#UnionDefaultGraph"),
    REQUIRESDATASET("http://www.w3.org/ns/sparql-service-description#RequiresDataset"),
    EMPTYGRAPHS("http://www.w3.org/ns/sparql-service-description#EmptyGraphs"),
    BASICFEDERATEDQUERY("http://www.w3.org/ns/sparql-service-description#BasicFederatedQuery"),
    ;

    private final IRI iri;
    SD(final String iri) {
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
