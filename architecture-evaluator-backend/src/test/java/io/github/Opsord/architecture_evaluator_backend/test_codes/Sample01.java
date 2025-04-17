package io.github.Opsord.architecture_evaluator_backend.test_codes;

public class Sample01 {
    /**
     * This is a sample code with CC = 5
     * Lines of Code = 6 (6 from foo method + 2 from the main method)
     */
    void foo() {
        // Variable declarations
        String n = "a";
        String n1 = "b";
        String n2 = "c";
        int age = 12;

        // Conditional block (if-condition)
        if (n.equals(n1) || n.equals(n2) || n1.equals(n1) && age != 23) {
            System.out.println("Inside the if condition.");
        }

        // Method body continuation
        System.out.println("Inside the foo method.");
    }

    // Main method
    public static void main(String[] args) {
        // Instantiating the class Sample01
        Sample01 obj = new Sample01();

        // Calling the foo method
        obj.foo();
    }
}