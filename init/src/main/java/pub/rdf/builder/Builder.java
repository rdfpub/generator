package pub.rdf.builder;

import pub.rdf.resource.ResourceHandler;

public interface Builder extends ResourceHandler {
    void init() throws Exception;
    void complete() throws Exception;
    void always() throws Exception;
}
