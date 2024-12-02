package pl.bst.client;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import pl.bst.protocol.*;

/** Requests are serialized; close can interrupt a request without acquiring its monitor. */
public final class TreeConnection implements AutoCloseable {
    private final Socket socket;
    private final BufferedReader reader;
    private final BufferedWriter writer;
    private final AtomicBoolean closed = new AtomicBoolean();
    private TreeConnection(Socket socket) throws IOException {
        this.socket = socket;
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
    }
    public static TreeConnection connect(String host, int port, TreeType type) throws IOException {
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(host, port), 3000); socket.setSoTimeout(5000);
            var connection = new TreeConnection(socket);
            String greeting = connection.request(type.name());
            if (!greeting.equals("Set tree type to " + type)) throw new IOException(greeting);
            return connection;
        } catch (IOException | RuntimeException error) { try { socket.close(); } catch(IOException ignored) { } throw error; }
    }
    public synchronized String request(String command) throws IOException {
        if (closed.get()) throw new IOException("Connection is closed");
        LineProtocol.write(writer, command);
        String response = LineProtocol.read(reader, 1_048_576);
        if (response == null) throw new EOFException("Server closed the connection");
        return response;
    }
    @Override public void close() {
        if (closed.compareAndSet(false, true)) try { socket.close(); } catch (IOException ignored) { }
    }
}
