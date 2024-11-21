package pl.bst.protocol;

import pl.bst.model.BinarySearchTree;

/** Sessions of the same type share a tree; the three types remain isolated. */
public final class TreeRegistry {
    private final BinarySearchTree<Integer> integers = new BinarySearchTree<>();
    private final BinarySearchTree<Double> doubles = new BinarySearchTree<>();
    private final BinarySearchTree<String> strings = new BinarySearchTree<>();
    public TreeCommandProcessor<?> open(TreeType type) {
        return switch (type) {
            case INTEGER -> new TreeCommandProcessor<>(integers, Integer::parseInt);
            case DOUBLE -> new TreeCommandProcessor<>(doubles, TreeType::finiteDouble);
            case STRING -> new TreeCommandProcessor<>(strings, value -> value);
        };
    }
}
