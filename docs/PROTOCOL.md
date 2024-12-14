# TCP protocol

UTF-8 text, one command and one response per line. The first request selects `Integer`, `Double` or `String`;
the server replies `Set tree type to INTEGER` (or the corresponding type). Unknown types return an error and close the connection.

| Request | Response |
| --- | --- |
| `insert 8` | `Inserted: 8` or `Already present: 8` |
| `delete 8` | `Deleted: 8` or `Not found: 8` |
| `search 8` | `Search 8: found` or `Search 8: not found` |
| `draw` | One preorder line; an empty tree produces one empty line |
| `bye` | `Bye`, then connection closure |

Commands are case insensitive. Values are single tokens. Integers use Java's integer range;
doubles must be finite; strings may contain Unicode but no whitespace. Invalid commands return `ERROR ...`
and keep the selected session available. Lines end with LF or CRLF. Request lines are limited to 4096 characters;
client response lines are limited to 1,048,576 characters.

There is no `END` marker after `draw`. Both clients consume exactly one response line.
Trees are shared by clients of the same type and are kept in memory until server shutdown.
Each BST operation is synchronized. A snapshot is captured under the same lock.

The desktop client performs connection and request work on a dedicated executor. Controls are disabled during a request;
drawing uses server data, and reconnecting clears the previous view. Socket closure cancels blocked reads during shutdown.
Client requests use a ReentrantLock rather than holding an intrinsic monitor during blocking I/O,
so virtual-thread callers do not pin all available scheduler carriers on Java 21.
