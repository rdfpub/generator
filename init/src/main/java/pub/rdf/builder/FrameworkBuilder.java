package pub.rdf.builder;

import pub.rdf.config.RDFPUBConfig;
import pub.rdf.resource.RDFPUBResource;
import pub.rdf.resource.ResourceFile;
import pub.rdf.util.Program;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.time.Instant;

public class FrameworkBuilder extends ConfigurableBuilder {
    private final Instant start = Instant.now();

    public FrameworkBuilder(final RDFPUBConfig config) {
        super(config);
    }

    @Override
    public void init() throws Exception {
        // Output beginning of build status
        Program.out("Beginning build");
        Program.out("Input directory is %s",config.getInputDirectory());
        Program.out("Output directory is %s",config.getOutputDirectory());
        Program.out("Base URI is %s",config.getBaseURI());

        // Get output directory and deletion policy
        final boolean deleteOutputDirectoryBeforeBuild = config.cleanOutputDirectory();

        // Initialize output directory
        try {
            // Determine output directory action
            if(deleteOutputDirectoryBeforeBuild) {
                Program.out("Output directory will be deleted and recreated before proceeding");
            } else {
                Program.out("Checking to ensure that output directory is empty");
            }

            // Walk through files either for deletion or for emptiness check
            Files.walkFileTree(config.getOutputDirectory(), new FileVisitor<>() {
                boolean isEmpty = true;

                @Override
                public FileVisitResult preVisitDirectory(final Path path, final BasicFileAttributes basicFileAttributes) {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(final Path path, final BasicFileAttributes basicFileAttributes) throws IOException {
                    if(deleteOutputDirectoryBeforeBuild) {
                        Files.delete(path);
                    } else {
                        throw new IOException("Output directory is not empty",new DirectoryNotEmptyException("Directory is not empty"));
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(final Path path, final IOException e) throws IOException {
                    if (e instanceof NoSuchFileException) {
                        return FileVisitResult.CONTINUE;
                    } else {
                        throw new IOException("Failed to delete output directory during initialization", e);
                    }
                }

                @Override
                public FileVisitResult postVisitDirectory(final Path path, final IOException e) throws IOException {
                    if(deleteOutputDirectoryBeforeBuild) {
                        Files.delete(path);
                    } else {
                        if (isEmpty) {
                            isEmpty = false;
                        } else {
                            throw new DirectoryNotEmptyException("Output directory is not empty");
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        catch(final NoSuchFileException e) {
            // ignore this exception, directory will get created
        }
        catch(final IOException e) {
            throw new IOException("Error while deleting output directory",e);
        }

        try {
            Files.createDirectory(config.getOutputDirectory());
        } catch (final FileAlreadyExistsException e) {
            // ignore this exception, we've already verified that it's empty
            Program.out("Output directory already exists, is empty");
        } catch (final IOException e) {
            throw new IOException("Error while creating output directory", e);
        }
    }

    @Override
    public void complete() {
        // noop
    }

    @Override
    public void always() {
        Program.out("Build completed in %ss", Duration.between(start,Instant.now()).toMillis() / 1000.0);
    }

    @Override
    public Exception handleNewResource(final RDFPUBResource resource) {
        Program.out("Entering resource directory %s",resource.getResourcePath());
        return null;
    }

    @Override
    public Exception handleResourceFile(final RDFPUBResource resource, final ResourceFile file) {
        Program.out("Scanning resource file %s", file);
        return null;
    }

    @Override
    public Exception handleFinishedResource(final RDFPUBResource resource) {
        Program.out("Finishing scan of resource directory %s",resource.getResourcePath());
        return null;
    }
}
