# Binary Search Tree — Java TCP Client & Server

A Java 21 networking project with a concurrent TCP server, a console client and a JavaFX tree visualizer.
Multiple clients can insert, search, delete and draw values in shared binary search trees.

## Features

- Independent Integer, Double and String trees, shared by sessions of the same type.
- A generic, synchronized BST with set semantics and immutable snapshots.
- One virtual thread per TCP session and explicit socket shutdown.
- Console commands and a JavaFX client with tree rendering, panning and scroll-wheel zoom.
- UTF-8 communication, request timeouts and bounded protocol lines.
- Network work outside the JavaFX thread; the display is refreshed from server responses.

## Build and run

Requirements: JDK 21 and Maven 3.8+. The JavaFX client additionally needs a graphical desktop.

```shell
mvn clean verify
```

Start the server in one terminal:

```shell
mvn -Pserver exec:java
```

Start the graphical client in another terminal:

```shell
mvn javafx:run
```

Or use the console client:

```shell
mvn -Pconsole exec:java
```

The default endpoint is `127.0.0.1:4444`. The GUI exposes host and port fields.
The server accepts optional arguments `<port> <bind-address>`; the console client accepts `<host> <port>`.
For example: `mvn -Pserver exec:java -Dexec.args="5555 127.0.0.1"`.
Open `pom.xml` as a Maven project in IntelliJ and select JDK 21; no preview flags or manually installed JavaFX SDK are needed.

## Commands

Select a tree type when connecting, then use:

| Command | Action |
| --- | --- |
| `insert 8` | Add a distinct value |
| `search 8` | Check membership |
| `delete 8` | Delete a value if present |
| `draw` | Return a single preorder line, empty for an empty tree |
| `bye` | Receive a farewell and close the connection |

Strings are single tokens and support Unicode. Double values must be finite.
The GUI locks the selected type while connected and fetches server data after operations; reconnecting clears the old view.
Use **Disconnect** to select a different type. The protocol is documented in [docs/PROTOCOL.md](docs/PROTOCOL.md).

## Structure

| Package | Responsibility |
| --- | --- |
| [model](src/main/java/pl/bst/model/) | Generic BST and immutable node snapshots |
| [protocol](src/main/java/pl/bst/protocol/) | Typed sessions, command parsing and line framing |
| [server](src/main/java/pl/bst/server/) | Listener, session workers and lifecycle |
| [client](src/main/java/pl/bst/client/) | Socket connection, console client and snapshot decoding |
| [ui](src/main/java/pl/bst/ui/) | JavaFX controls and iterative tree rendering |
| [app](src/main/java/pl/bst/app/) | JavaFX application entry point |
| [examples](src/main/java/pl/bst/examples/) | Original standalone byte-stream exercise |

## Validation and boundaries

Tests cover deletion cases, skewed trees, concurrent mutations, real loopback TCP clients, shared/type-isolated sessions,
malformed input, empty `draw` responses and shutdown. The desktop smoke test connects the real JavaFX window to a server.
Tests constrain the virtual-thread scheduler to two carrier threads to catch blocking-under-monitor regressions.
CI runs the complete suite under Xvfb; desktop tests are skipped locally when `DISPLAY` is absent.

This is an educational, in-memory server. Trees are unbalanced, so operations can take linear time on skewed inputs.
Server restarts clear the data. Authentication, durable storage and encrypted transport are future extensions.

## History

Reorganized from [serwer](https://github.com/jedreekPrograms/serwer), including its console, server, GUI and standalone stream exercise.
The staged history was reconstructed from imported snapshots; assigned November–December 2024 dates are not recovered original timestamps.
