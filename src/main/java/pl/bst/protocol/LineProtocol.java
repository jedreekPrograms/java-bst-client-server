package pl.bst.protocol;

import java.io.*;
public final class LineProtocol {
    private LineProtocol() { }
    public static String read(BufferedReader reader, int maximum) throws IOException { return reader.readLine(); }
    public static void write(BufferedWriter writer, String line) throws IOException {
        writer.write(line); writer.newLine(); writer.flush();
    }
}
