package org.avasthi.java.cli;

interface I1 {
    int i1 = 10;
    default void sayHello() {
        System.out.println("Hello World");
    }
    default void sayHello(String name) {
        System.out.println("Hello " + name);
    }
}

public class InterfaceExample implements I1 {
    public void sayHello() {
        System.out.println("HELLO World " + i1);
    }
    public void sayHello(String name) {
        System.out.println("HELLO " + name + " " + i1);
    }
    public static void main(String[] args) {
        InterfaceExample ie = new InterfaceExample();
        ie.sayHello();
        ie.sayHello("Vinay");
    }
}
