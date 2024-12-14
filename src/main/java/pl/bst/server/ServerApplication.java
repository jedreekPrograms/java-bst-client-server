package pl.bst.server;

import java.net.InetAddress;
public final class ServerApplication {
    private ServerApplication() { }
    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 4444;
        var address = InetAddress.getByName(args.length > 1 ? args[1] : "127.0.0.1");
        try (var server = new TreeServer(port, address)) {
            Thread shutdown = new Thread(server::close, "bst-shutdown");
            Runtime.getRuntime().addShutdownHook(shutdown);
            server.start();
            System.out.println("BST server listening on " + address.getHostAddress() + ":" + server.port());
            server.await();
        }
    }
}
