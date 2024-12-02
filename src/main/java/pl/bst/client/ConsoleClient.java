package pl.bst.client;

import java.io.*;
import pl.bst.protocol.TreeType;

public final class ConsoleClient {
    private ConsoleClient() { }
    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "127.0.0.1";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 4444;
        var console = new BufferedReader(new InputStreamReader(System.in, java.nio.charset.StandardCharsets.UTF_8));
        try {
            System.out.print("Tree type (Integer, Double, String): ");
            String selected = console.readLine(); if (selected == null) return;
            try (var connection = TreeConnection.connect(host, port, TreeType.parse(selected))) {
                System.out.println("Connected. Commands: insert, delete, search, draw, bye");
                String command;
                while ((command = console.readLine()) != null) {
                    String response = connection.request(command);
                    System.out.println(response);
                    if (response.equals("Bye")) break;
                }
            }
        } catch (IOException | IllegalArgumentException error) { System.err.println("Client error: " + error.getMessage()); }
    }
}
