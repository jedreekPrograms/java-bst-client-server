package pl.bst.server;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.*;
import pl.bst.protocol.*;

public final class TreeServer implements AutoCloseable {
    private final ServerSocket listener = new ServerSocket();
    private final TreeRegistry registry = new TreeRegistry();
    private final ExecutorService sessions = Executors.newVirtualThreadPerTaskExecutor();
    private final Set<Socket> clients = ConcurrentHashMap.newKeySet();
    private Thread acceptor;
    private volatile boolean running;
    private boolean started, closed;

    public TreeServer(int port) throws IOException { this(port, InetAddress.getLoopbackAddress()); }
    public TreeServer(int port, InetAddress address) throws IOException {
        listener.bind(new InetSocketAddress(address, port));
    }
    public int port() { return listener.getLocalPort(); }
    public synchronized void start() {
        if (started || closed) throw new IllegalStateException("Server cannot be restarted");
        started = true; running = true;
        acceptor = Thread.ofPlatform().daemon(true).name("bst-acceptor").start(this::accept);
    }
    private void accept() {
        while (running) {
            try {
                Socket socket = listener.accept(); socket.setTcpNoDelay(true);
                synchronized (this) {
                    if (!running) { socket.close(); break; }
                    clients.add(socket);
                    sessions.submit(() -> serve(socket));
                }
            } catch (IOException failure) {
                if (running) System.err.println("Accept failed: " + failure.getMessage());
            }
        }
    }
    private void serve(Socket socket) {
        try (socket;
             var reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             var writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {
            String typeLine = LineProtocol.read(reader, 4096);
            if (typeLine == null) return;
            TreeType type;
            try { type = TreeType.parse(typeLine); }
            catch (IllegalArgumentException invalid) { LineProtocol.write(writer, "ERROR Unknown tree type"); return; }
            var processor = registry.open(type);
            LineProtocol.write(writer, "Set tree type to " + type);
            String line;
            while ((line = LineProtocol.read(reader, 4096)) != null) {
                String response = processor.execute(line);
                LineProtocol.write(writer, response);
                if (response.equals("Bye")) return;
            }
        } catch (IOException disconnected) {
            // Closing the server also interrupts blocked session reads by closing sockets.
        } finally { clients.remove(socket); }
    }
    public void await() throws InterruptedException {
        Thread thread;
        synchronized (this) { thread = acceptor; }
        if (thread != null) thread.join();
    }
    @Override public synchronized void close() {
        closed = true; running = false;
        try { listener.close(); } catch (IOException ignored) { }
        for (Socket socket : clients) try { socket.close(); } catch (IOException ignored) { }
        sessions.shutdownNow();
        if (acceptor != null) acceptor.interrupt();
    }
}
