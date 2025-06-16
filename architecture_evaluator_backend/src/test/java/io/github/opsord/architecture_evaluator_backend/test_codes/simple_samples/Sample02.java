package io.github.opsord.architecture_evaluator_backend.test_codes.simple_samples;

public class Sample02 {
    /**
     * This is a sample code with multiple if-else conditions
     */
    void foo() {
        // Variable declaration
        int y = 0;

        // First if-condition
        if (true) {
            System.out.println("Inside the first if-condition.");
        }

        // Second if-condition
        if (true && false) {
            y = 1;
            System.out.println("Inside the second if-condition.");
        }
        // Else block
        else {
            y = 2;
        }

        // Method body continuation
        System.out.println("Inside the foo method.");
    }

    // Main method
    public static void main(String[] args) {
        // Instantiating the class Sample02
        Sample02 obj = new Sample02();

        // Calling the foo method
        obj.foo();
    }
}