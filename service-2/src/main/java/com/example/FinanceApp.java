package com.example;

public class FinanceApp {
    public static void main(String[] args) {

        int n1 = 100;
        int n2 = 200;

        Calculator calculator = new Calculator();
        int sum = calculator.add(n1, n2);
        System.out.println("The sum of " + n1 + " and " + n2 + " is: " + sum);


    }
}
