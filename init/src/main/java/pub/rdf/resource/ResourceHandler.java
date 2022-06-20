package pub.rdf.resource;

public interface ResourceHandler {
    Exception handleNewResource(RDFPUBResource resource);
    Exception handleResourceFile(RDFPUBResource resource, ResourceFile file);
    Exception handleFinishedResource(RDFPUBResource resource);
}
