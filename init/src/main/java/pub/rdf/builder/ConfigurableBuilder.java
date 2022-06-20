package pub.rdf.builder;

import pub.rdf.config.RDFPUBConfig;

public abstract class ConfigurableBuilder implements Builder {
    protected final RDFPUBConfig config;

    protected ConfigurableBuilder(final RDFPUBConfig config) {
        this.config = config;
    }
}
