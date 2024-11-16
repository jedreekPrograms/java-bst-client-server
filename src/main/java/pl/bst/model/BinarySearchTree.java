package pl.bst.model;

import java.util.*;

/** An unbalanced BST with set semantics. One monitor guards every operation. */
public final class BinarySearchTree<T extends Comparable<? super T>> {
    private static final class Node<T> {
        T value; Node<T> left, right;
        Node(T value) { this.value = value; }
    }
    private Node<T> root;
    private int size;

    public synchronized boolean insert(T value) {
        Objects.requireNonNull(value);
        if (root == null) { root = new Node<>(value); size++; return true; }
        Node<T> current = root;
        while (true) {
            int comparison = value.compareTo(current.value);
            if (comparison == 0) return false;
            if (comparison < 0) {
                if (current.left == null) { current.left = new Node<>(value); break; }
                current = current.left;
            } else {
                if (current.right == null) { current.right = new Node<>(value); break; }
                current = current.right;
            }
        }
        size++; return true;
    }

    public synchronized boolean search(T value) {
        Objects.requireNonNull(value);
        Node<T> current = root;
        while (current != null) {
            int comparison = value.compareTo(current.value);
            if (comparison == 0) return true;
            current = comparison < 0 ? current.left : current.right;
        }
        return false;
    }

    public synchronized int size() { return size; }
    public synchronized void clear() { root = null; size = 0; }
}
