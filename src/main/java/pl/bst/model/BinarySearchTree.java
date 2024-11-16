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

    public synchronized boolean delete(T value) {
        Objects.requireNonNull(value);
        Node<T> parent = null, current = root;
        while (current != null && value.compareTo(current.value) != 0) {
            parent = current;
            current = value.compareTo(current.value) < 0 ? current.left : current.right;
        }
        if (current == null) return false;
        if (current.left != null && current.right != null) {
            Node<T> successorParent = current, successor = current.right;
            while (successor.left != null) { successorParent = successor; successor = successor.left; }
            current.value = successor.value;
            parent = successorParent; current = successor;
        }
        Node<T> child = current.left != null ? current.left : current.right;
        if (parent == null) root = child;
        else if (parent.left == current) parent.left = child;
        else parent.right = child;
        size--; return true;
    }

    public synchronized List<T> preOrder() {
        List<T> values = new ArrayList<>();
        if (root == null) return List.of();
        Deque<Node<T>> pending = new ArrayDeque<>(); pending.push(root);
        while (!pending.isEmpty()) {
            Node<T> node = pending.pop(); values.add(node.value);
            if (node.right != null) pending.push(node.right);
            if (node.left != null) pending.push(node.left);
        }
        return List.copyOf(values);
    }

    public synchronized String toPreOrderString() {
        return String.join(" ", preOrder().stream().map(Object::toString).toList());
    }

    public synchronized TreeNode<T> snapshot() {
        if (root == null) return null;
        List<Node<T>> order = new ArrayList<>();
        Deque<Node<T>> pending = new ArrayDeque<>(); pending.push(root);
        while (!pending.isEmpty()) {
            Node<T> node = pending.pop(); order.add(node);
            if (node.left != null) pending.push(node.left);
            if (node.right != null) pending.push(node.right);
        }
        Map<Node<T>, TreeNode<T>> copies = new IdentityHashMap<>();
        for (int i = order.size() - 1; i >= 0; i--) {
            Node<T> node = order.get(i);
            copies.put(node, new TreeNode<>(node.value, copies.get(node.left), copies.get(node.right)));
        }
        return copies.get(root);
    }

    public synchronized int size() { return size; }
    public synchronized void clear() { root = null; size = 0; }
}
