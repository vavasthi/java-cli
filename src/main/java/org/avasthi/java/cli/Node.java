package org.avasthi.java.cli;

public class Node {
    public Node(int value, Node next, Node other) {
        this.value = value;
        this.next = next;
        this.other = other;
    }

    public Node(int value) {
        this.value = value;
    }

    int value;
    Node next;
    Node other;
}
