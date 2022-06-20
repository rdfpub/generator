package pub.rdf.util;

import java.util.regex.Pattern;

public final class Regex {
    public final static Pattern LANGUAGE = Pattern.compile("^[a-zA-Z]{1,8}(?:-[a-zA-Z0-9]{1,8})*$");
    public final static Pattern HTTP_S_URL = Pattern.compile("^https?://[a-zA-Z0-9]");
}
