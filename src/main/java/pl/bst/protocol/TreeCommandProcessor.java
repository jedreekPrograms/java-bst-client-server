package pl.bst.protocol;

import java.util.Locale;
import java.util.function.Function;
import pl.bst.model.BinarySearchTree;

public final class TreeCommandProcessor<T extends Comparable<? super T>> {
    private final BinarySearchTree<T> tree;
    private final Function<String,T> parser;
    public TreeCommandProcessor(BinarySearchTree<T> tree, Function<String,T> parser) {
        this.tree = tree; this.parser = parser;
    }
    public String execute(String line) {
        if (line == null || line.isBlank()) return "ERROR Empty command";
        String[] parts = line.trim().split("\\s+", 2);
        String command = parts[0].toLowerCase(Locale.ROOT);
        String value = parts.length == 2 ? parts[1].trim() : "";
        if (command.equals("draw") || command.equals("bye")) {
            if (!value.isEmpty()) return "ERROR Unexpected argument";
            return command.equals("draw") ? tree.toPreOrderString() : "Bye";
        }
        if (!command.equals("insert") && !command.equals("delete") && !command.equals("search"))
            return "ERROR Unknown command";
        if (value.isEmpty() || value.chars().anyMatch(Character::isWhitespace)) return "ERROR Expected one value";
        try {
            T parsed = parser.apply(value);
            return switch (command) {
                case "insert" -> (tree.insert(parsed) ? "Inserted: " : "Already present: ") + value;
                case "delete" -> (tree.delete(parsed) ? "Deleted: " : "Not found: ") + value;
                default -> "Search " + value + ": " + (tree.search(parsed) ? "found" : "not found");
            };
        } catch (IllegalArgumentException invalid) { return "ERROR Invalid value: " + value; }
    }
}
