package pub.rdf.util;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

public enum DCTerms implements IRI {
    AGENT("http://purl.org/dc/terms/Agent"),
    AGENTCLASS("http://purl.org/dc/terms/AgentClass"),
    BIBLIOGRAPHICRESOURCE("http://purl.org/dc/terms/BibliographicResource"),
    BOX("http://purl.org/dc/terms/Box"),
    DCMITYPE("http://purl.org/dc/terms/DCMIType"),
    DDC("http://purl.org/dc/terms/DDC"),
    FILEFORMAT("http://purl.org/dc/terms/FileFormat"),
    FREQUENCY("http://purl.org/dc/terms/Frequency"),
    IMT("http://purl.org/dc/terms/IMT"),
    ISO3166("http://purl.org/dc/terms/ISO3166"),
    ISO639_2("http://purl.org/dc/terms/ISO639-2"),
    ISO639_3("http://purl.org/dc/terms/ISO639-3"),
    JURISDICTION("http://purl.org/dc/terms/Jurisdiction"),
    LCC("http://purl.org/dc/terms/LCC"),
    LCSH("http://purl.org/dc/terms/LCSH"),
    LICENSEDOCUMENT("http://purl.org/dc/terms/LicenseDocument"),
    LINGUISTICSYSTEM("http://purl.org/dc/terms/LinguisticSystem"),
    LOCATION("http://purl.org/dc/terms/Location"),
    LOCATIONPERIODORJURISDICTION("http://purl.org/dc/terms/LocationPeriodOrJurisdiction"),
    MESH("http://purl.org/dc/terms/MESH"),
    MEDIATYPE("http://purl.org/dc/terms/MediaType"),
    MEDIATYPEOREXTENT("http://purl.org/dc/terms/MediaTypeOrExtent"),
    METHODOFACCRUAL("http://purl.org/dc/terms/MethodOfAccrual"),
    METHODOFINSTRUCTION("http://purl.org/dc/terms/MethodOfInstruction"),
    NLM("http://purl.org/dc/terms/NLM"),
    PERIOD("http://purl.org/dc/terms/Period"),
    PERIODOFTIME("http://purl.org/dc/terms/PeriodOfTime"),
    PHYSICALMEDIUM("http://purl.org/dc/terms/PhysicalMedium"),
    PHYSICALRESOURCE("http://purl.org/dc/terms/PhysicalResource"),
    POINT("http://purl.org/dc/terms/Point"),
    POLICY("http://purl.org/dc/terms/Policy"),
    PROVENANCESTATEMENT("http://purl.org/dc/terms/ProvenanceStatement"),
    RFC1766("http://purl.org/dc/terms/RFC1766"),
    RFC3066("http://purl.org/dc/terms/RFC3066"),
    RFC4646("http://purl.org/dc/terms/RFC4646"),
    RFC5646("http://purl.org/dc/terms/RFC5646"),
    RIGHTSSTATEMENT("http://purl.org/dc/terms/RightsStatement"),
    SIZEORDURATION("http://purl.org/dc/terms/SizeOrDuration"),
    STANDARD("http://purl.org/dc/terms/Standard"),
    TGN("http://purl.org/dc/terms/TGN"),
    UDC("http://purl.org/dc/terms/UDC"),
    URI("http://purl.org/dc/terms/URI"),
    W3CDTF("http://purl.org/dc/terms/W3CDTF"),

    ABSTRACT("http://purl.org/dc/terms/abstract"),
    ACCESSRIGHTS("http://purl.org/dc/terms/accessRights"),
    ACCRUALMETHOD("http://purl.org/dc/terms/accrualMethod"),
    ACCRUALPERIODICITY("http://purl.org/dc/terms/accrualPeriodicity"),
    ACCRUALPOLICY("http://purl.org/dc/terms/accrualPolicy"),
    ALTERNATIVE("http://purl.org/dc/terms/alternative"),
    AUDIENCE("http://purl.org/dc/terms/audience"),
    AVAILABLE("http://purl.org/dc/terms/available"),
    BIBLIOGRAPHICCITATION("http://purl.org/dc/terms/bibliographicCitation"),
    CONFORMSTO("http://purl.org/dc/terms/conformsTo"),
    CONTRIBUTOR("http://purl.org/dc/terms/contributor"),
    COVERAGE("http://purl.org/dc/terms/coverage"),
    CREATED("http://purl.org/dc/terms/created"),
    CREATOR("http://purl.org/dc/terms/creator"),
    DATE("http://purl.org/dc/terms/date"),
    DATEACCEPTED("http://purl.org/dc/terms/dateAccepted"),
    DATECOPYRIGHTED("http://purl.org/dc/terms/dateCopyrighted"),
    DATESUBMITTED("http://purl.org/dc/terms/dateSubmitted"),
    DESCRIPTION("http://purl.org/dc/terms/description"),
    EDUCATIONLEVEL("http://purl.org/dc/terms/educationLevel"),
    EXTENT("http://purl.org/dc/terms/extent"),
    FORMAT("http://purl.org/dc/terms/format"),
    HASFORMAT("http://purl.org/dc/terms/hasFormat"),
    HASPART("http://purl.org/dc/terms/hasPart"),
    HASVERSION("http://purl.org/dc/terms/hasVersion"),
    IDENTIFIER("http://purl.org/dc/terms/identifier"),
    INSTRUCTIONALMETHOD("http://purl.org/dc/terms/instructionalMethod"),
    ISFORMATOF("http://purl.org/dc/terms/isFormatOf"),
    ISPARTOF("http://purl.org/dc/terms/isPartOf"),
    ISREFERENCEDBY("http://purl.org/dc/terms/isReferencedBy"),
    ISREPLACEDBY("http://purl.org/dc/terms/isReplacedBy"),
    ISREQUIREDBY("http://purl.org/dc/terms/isRequiredBy"),
    ISVERSIONOF("http://purl.org/dc/terms/isVersionOf"),
    ISSUED("http://purl.org/dc/terms/issued"),
    LANGUAGE("http://purl.org/dc/terms/language"),
    LICENSE("http://purl.org/dc/terms/license"),
    MEDIATOR("http://purl.org/dc/terms/mediator"),
    MEDIUM("http://purl.org/dc/terms/medium"),
    MODIFIED("http://purl.org/dc/terms/modified"),
    PROVENANCE("http://purl.org/dc/terms/provenance"),
    PUBLISHER("http://purl.org/dc/terms/publisher"),
    REFERENCES("http://purl.org/dc/terms/references"),
    RELATION("http://purl.org/dc/terms/relation"),
    REPLACES("http://purl.org/dc/terms/replaces"),
    REQUIRES("http://purl.org/dc/terms/requires"),
    RIGHTS("http://purl.org/dc/terms/rights"),
    RIGHTSHOLDER("http://purl.org/dc/terms/rightsHolder"),
    SOURCE("http://purl.org/dc/terms/source"),
    SPATIAL("http://purl.org/dc/terms/spatial"),
    SUBJECT("http://purl.org/dc/terms/subject"),
    TABLEOFCONTENTS("http://purl.org/dc/terms/tableOfContents"),
    TEMPORAL("http://purl.org/dc/terms/temporal"),
    TITLE("http://purl.org/dc/terms/title"),
    TYPE("http://purl.org/dc/terms/type"),
    VALID("http://purl.org/dc/terms/valid");

    private IRI iri;
    DCTerms(final String iri) {
        this.iri = SimpleValueFactory.getInstance().createIRI(iri);
    }

    @Override
    public String getNamespace() {
        return iri.getNamespace();
    }

    @Override
    public String getLocalName() {
        return iri.getLocalName();
    }

    @Override
    public String stringValue() {
        return iri.stringValue();
    }

    @Override
    public String toString() {
        return this.stringValue();
    }
}