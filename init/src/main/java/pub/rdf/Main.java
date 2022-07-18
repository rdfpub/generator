package pub.rdf;

import pub.rdf.builder.*;
import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import pub.rdf.config.RDFPUBConfig;
import pub.rdf.resource.RDFPUBResource;
import pub.rdf.resource.ResourceFile;
import pub.rdf.util.Program;
import pub.rdf.util.Regex;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.regex.Pattern;

import static pub.rdf.util.Program.out;

public class Main {
    public static void main(final String[] args) throws Exception {
        // Check for input and output directories
        if(args.length < 2) {
            Program.xerr("init requires two arguments: inputdir and outputdir",new IllegalArgumentException("Too few program arguments supplied"));
        }

        final Path inputdir = Path.of(args[0]);
        final Path outputdir = Path.of(args[1]);

        if(!Files.exists(inputdir)) {
            Program.xerr("Input directory does not exist.",new IllegalArgumentException("Input directory must exist"));
        }

        // Load configuration from input directory
        final RDFPUBConfig config;
        try {
            config = RDFPUBConfig.load(inputdir,outputdir);
        } catch(final FileNotFoundException e) {
            Program.xerr("No .rdfpub file found; it must exist and contain rdfpub site configuration",new IllegalStateException("No .rdfpub file found"));
            return;
        } catch(final IOException e) {
            Program.xerr("Error reading .rdfpub file",e);
            return;
        }

        // Validate configuration
        if(config.getDefaultLanguage() == null) {
            Program.xerr("No default language specified; DefaultLanguage must be set in .rdfpub",new IllegalStateException("No default language specified"));
        }

        if(!Regex.LANGUAGE.matcher(config.getDefaultLanguage()).find()) {
            Program.xerr("Default language does not conform to a language code (ex: en, en-us, es, tr)",new IllegalStateException("Invalid default language specified"));
        }

        Program.out("Default language is '%s'",config.getDefaultLanguage());

        if(config.getBaseURI() == null) {
            Program.xerr("No base URI specified; BaseURI must be set in .rdfpub",new IllegalStateException("No base URI specified"));
        }

        if(!Regex.HTTP_S_URL.matcher(config.getBaseURI().toString()).find()) {
            Program.xerr("Base URI must be an http: or https: URL",new IllegalStateException("Invalid base URI specified"));
        }

        Program.out("Base URI is <%s>",config.getBaseURI());

        // Add feature sets
        final Builder[] builders = new Builder[] {
            new FrameworkBuilder(config),
            new ResourceFileBuilder(config),
            new DatabaseBuilder(config),
            new NginxConfigBuilder(config)
        };

        // Initialize features
        for(final Builder builder : builders) {
            builder.init();
        }

        // Initialize list of possible build errors
        final List<Exception> buildErrors = new LinkedList<>();

        // Initialize regex for included and excluded files
        final Pattern includes;
        if(!config.getIncludeFiles().isEmpty()) {
            final StringBuilder includestr = new StringBuilder(256);
            for (final String include : config.getIncludeFiles()) {
                includestr.append("(?:").append(include).append(")|");
            }
            includes = Pattern.compile(includestr.deleteCharAt(includestr.length() - 1).toString());
        } else {
            includes = null;
        }

        final Pattern excludes;
        if(!config.getExcludeFiles().isEmpty()) {
            final StringBuilder excludestr = new StringBuilder(256);
            for (final String exclude : config.getExcludeFiles()) {
                excludestr.append("(?:").append(exclude).append(")|");
            }
            excludes = Pattern.compile(excludestr.deleteCharAt(excludestr.length() - 1).toString());
        } else {
            excludes = null;
        }

        try {
            // Traverse and process the input directory
            final FileVisitor<Path> directoryLoader = new SimpleFileVisitor<>() {
                private final Deque<RDFPUBResource> resources = new LinkedList<>();

                private void checkBuildError(final Exception e) {
                    if(e != null) {
                        Program.err("ERROR: %s",e.getMessage());
                        e.printStackTrace();
                        buildErrors.add(e);
                    }
                }

                @Override
                public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) {
                    // Do inclusion/exclusion checks
                    final String dirstr = dir.toString();
                    if(includes != null && includes.matcher(dirstr).find()) {
                        out("Directory %s is explicitly included in the build",dirstr);
                    } else if (excludes != null && excludes.matcher(dirstr).find()) {
                        out("Directory %s is explicitly excluded from the build",dirstr);
                        return FileVisitResult.SKIP_SUBTREE;
                    } else if (dirstr.contains("/.")) {
                        out(String.format("Skipping dot directory %s", dir));
                        return FileVisitResult.SKIP_SUBTREE;
                    }

                    // Create resource for current path
                    final RDFPUBResource resource = new RDFPUBResource(config,config.getInputDirectory().relativize(dir));

                    // Builder processing
                    for (final Builder feature : builders) {
                        checkBuildError(feature.handleNewResource(resource));
                    }

                    resources.push(resource);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) {
                    // Do inclusion/exclusion checks
                    final String filestr = file.toString();
                    if(includes != null && includes.matcher(filestr).find()) {
                        out("File %s is explicitly included in the build",filestr);
                    } else if (excludes != null && excludes.matcher(filestr).find()) {
                        out("File %s is explicitly excluded from the build",filestr);
                        return FileVisitResult.CONTINUE;
                    } else if (filestr.contains("/.")) {
                        out(String.format("Skipping dot file %s", file));
                        return FileVisitResult.CONTINUE;
                    }

                    // Get relative path within input directory
                    final RDFPUBResource resource = resources.peek();
                    out(String.format("Current resource is %s",resource));

                    // Builder processing
                    for(final Builder builder : builders) {
                        checkBuildError(builder.handleResourceFile(resource,new ResourceFile(file)));
                    }

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(final Path file, final IOException e) {
                    // Collect exception
                    checkBuildError(e);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(final Path dir, final IOException e) {
                    final RDFPUBResource resource = resources.pop();

                    // Builder processing
                    for(final Builder builder : builders) {
                        checkBuildError(builder.handleFinishedResource(resource));
                    }

                    return FileVisitResult.CONTINUE;
                }
            };

            Files.walkFileTree(config.getInputDirectory(), directoryLoader);

            // Finish features
            for(final Builder builder : builders) {
                builder.complete();
            }
        } finally {
            // Trigger final processing for builders
            for(final Builder builder : builders) {
                builder.always();
            }

            // Evaluate build errors, if any
            if(!buildErrors.isEmpty()) {
                Program.out("%s build errors encountered.",buildErrors.size());
                for(final Exception e : buildErrors) {
                    Program.out("  %s",e.getMessage());
                }
                System.exit(1);
            } else {
                Program.out("No build errors! :)");
                System.exit(0);
            }
        }
    }
}
