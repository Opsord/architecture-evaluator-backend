package io.github.opsord.architecture_evaluator_backend.test_codes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Sample03.java
 * This is one of the codes used in the paper:
 * "An Improved Software Complexity Metric Based on Cyclomatic Complexity"
 * This program demonstrates a simple input validation for a code number.
 * It checks if the entered code is between 1000 and 9999.
 */

public class Sample03 {
    public static void main(String[] args) {
        try {
            int code;
            BufferedReader x;
            InputStreamReader i = new InputStreamReader(System.in);
            x = new BufferedReader(i);
            System.out.print("Enter your code:");
            code = Integer.parseInt(x.readLine());
            CheckAccess(code);
        } catch (IOException e) {
            System.err.println("Input error: " + e.getMessage());
        }
    }

    private static void CheckAccess(int code) {
        if (code >= 1000 && code < 10000) {
            System.out.println("Accepted");
        } else {
            System.out.println("Not Accepted");
        }
    }
}
