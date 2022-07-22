package pub.rdf.resource;

import java.nio.file.Path;

public class ResourceFile {
    private final Path path;
    private String name, extension, language;

    public ResourceFile(final Path path) {
        this.path = path;
        final String filename = path.getFileName().toString();
        final String tempname;

        if(filename.indexOf('.') != -1) {
            tempname = filename.substring(0,filename.lastIndexOf('.'));
            extension = filename.substring(filename.lastIndexOf('.') + 1);
        } else {
            tempname = filename;
            extension = null;
        }

        if(isTemplate() && tempname.startsWith("index@")) {
            name = tempname.substring(0, tempname.indexOf('@'));
            language = tempname.substring(tempname.indexOf('@') + 1);
        } else {
            name = tempname;
            language = null;
        }
    }

    public String getName() {
        return name;
    }

    public String getExtension() {
        return extension;
    }

    public String getLanguage() {
        return language;
    }

    public Path getPath() {
        return path;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setExtension(final String extension) {
        this.extension = extension;
    }

    public void setLanguage(final String language) {
        this.language = language;
    }

    public boolean isTemplate() {
        return "md".equals(extension) || "html".equals(extension);
    }

    public boolean isIndexTemplate() {
        return isTemplate() && "index".equals(name);
    }

    public String getFileName() {
        return name + (language == null ? "" : "@" + language) + (extension == null ? "" : "." + extension);
    }

    @Override
    public String toString() {
        return path.toString();
    }
}
