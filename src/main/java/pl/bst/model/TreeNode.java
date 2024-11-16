package pl.bst.model;

/** Immutable view of a tree node, safe to pass to the renderer. */
public record TreeNode<T>(T value, TreeNode<T> left, TreeNode<T> right) { }
