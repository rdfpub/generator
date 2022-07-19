package pub.rdf.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

public final class Program {
    private final static StringBuilder dateStringBuilder = new StringBuilder(32);
    private static void printDate(final PrintStream stream) {
        dateStringBuilder.setLength(0);
        stream.print(dateStringBuilder.append('[').append(Instant.now()).append(']').append(' '));
    }
    public static void out(String message, Object... objects) {
        printDate(System.out);
        System.out.printf(message + "%n",objects);
    }

    public static void err(String message, Object... objects) {
        printDate(System.err);
        System.err.printf(message + "%n",objects);
    }

    public static void xerr(final String why, final Exception e) {
        err(why);
        e.printStackTrace();
        System.exit(1);
    }

    public static String readResource(final String filename) {
        try(final InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename)) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            throw new RuntimeException(String.format("Error while attempting to read internal file %s",filename),e);
        } catch (final NullPointerException e) {
            throw new RuntimeException(String.format("Internal file %s not found",filename),e);
        }
    }
}
