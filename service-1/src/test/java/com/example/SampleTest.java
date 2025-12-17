package com.example;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class SampleTest {



    @Tag("finance")
    @ParameterizedTest
    @ValueSource(strings = {"UPI", "IMPS", "NEFT"})
    void validatePaymentMode(String mode) {
        assertTrue(true);
    }


    @Tag("finance")
    @ParameterizedTest
    @CsvFileSource(
            resources = "/transfer-data.csv",
            numLinesToSkip = 1
    )
    public void transfer(double amount,String fromAccount, String toAccount) {
        assertTrue(true);
    }


}
