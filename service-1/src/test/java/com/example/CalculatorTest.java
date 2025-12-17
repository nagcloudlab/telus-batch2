package com.example;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CalculatorTest {

    // Given | Arrange
    // When  | Act
    // Then  | Assert

    // Testing F.W :
    // java -  Junit | TestNG
    // JS   -  Mocha | Jest | Jasmine
    // Python -  PyTest
    // C#  -  NUnit | MSTest | xUnit


    public CalculatorTest(){
        System.out.println("CalculatorTest instance created");
    }


    @Tag("calc")
    @Test
    public void testAddition() {
        // TDD Approach
        // Given // Arrange
        Calculator calculator = new Calculator(); // Unit
        int a = 5;
        int b = 3;
        // When | Act
        int result = calculator.add(a, b);
        // Then | Assert
        assertEquals(8, result, "5 + 3 should equal 8");
    }

    @Tag("calc")
    @Test
    public void testSubtraction() {
        // Given // Arrange
        Calculator calculator = new Calculator(); // Unit
        int a = 10;
        int b = 4;
        // When | Act
        int result = calculator.subtract(a, b);
        // Then | Assert
        assertEquals(6, result, "10 - 4 should equal 6");
    }

    @Tag("calc")
    @Test
    public void testMultiplication() {
        // Given // Arrange
        Calculator calculator = new Calculator(); // Unit
        int a = 7;
        int b = 6;
        // When | Act
        int result = calculator.multiply(a, b);
        // Then | Assert
        assertEquals(42, result, "7 * 6 should equal 42");
    }

    @Tag("calc")
    @Test
    public void testDivision() {
        // Given // Arrange
        Calculator calculator = new Calculator(); // Unit
        int a = 20;
        int b = 5;
        // When | Act
        int result = calculator.divide(a, b);
        // Then | Assert
        assertEquals(4, result, "20 / 5 should equal 4");
    }

}
