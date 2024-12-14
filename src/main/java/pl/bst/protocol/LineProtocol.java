package pl.bst.protocol;

import java.io.*;
public final class LineProtocol {
    private LineProtocol() { }
    public static String read(BufferedReader reader, int maximum) throws IOException {
        StringBuilder line = new StringBuilder();
        int next;
        while ((next = reader.read()) != -1) {
            if (next == 10) return finish(line, maximum);
            if (line.length() > maximum) throw new IOException("Protocol line too long");
            line.append((char) next);
        }
        return line.isEmpty() ? null : finish(line, maximum);
    }
    private static String finish(StringBuilder line, int maximum) throws IOException {
        if (!line.isEmpty() && line.charAt(line.length() - 1) == 13) line.setLength(line.length() - 1);
        if (line.length() > maximum) throw new IOException("Protocol line too long");
        return line.toString();
    }
    public static void write(BufferedWriter writer, String line) throws IOException {
        writer.write(line); writer.newLine(); writer.flush();
    }
}
