package pl.bst.client;

import java.util.function.Function;
import pl.bst.model.*;
import pl.bst.protocol.TreeType;

public final class TreeDecoder {
    private TreeDecoder() { }
    public static TreeNode<?> decode(String preorder, TreeType type) {
        return switch (type) {
            case INTEGER -> decode(preorder, Integer::parseInt);
            case DOUBLE -> decode(preorder, TreeType::finiteDouble);
            case STRING -> decode(preorder, Function.identity());
        };
    }
    private static <T extends Comparable<? super T>> TreeNode<T> decode(String preorder, Function<String,T> parser) {
        var tree = new BinarySearchTree<T>();
        if (!preorder.isBlank()) for (String value : preorder.trim().split("\\s+")) tree.insert(parser.apply(value));
        return tree.snapshot();
    }
}
