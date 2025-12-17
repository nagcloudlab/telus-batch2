package com.example.model;

public class Account {

    private String number;
    private String holderName;
    private double balance;

    public Account(String number, String holderName, double balance) {
        this.number = number;
        this.holderName = holderName;
        this.balance = balance;
    }

    public String getNumber() {
        return number;
    }

    public String getHolderName() {
        return holderName;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    // debit
    public void debit(double amount) {
        if (amount > 0 && amount <= balance) {
            balance -= amount;
        } else {
            throw new IllegalArgumentException("Invalid debit amount");
        }
    }

    // credit
    public void credit(double amount) {
        if (amount > 0) {
            balance += amount;
        } else {
            throw new IllegalArgumentException("Invalid credit amount");
        }
    }


}
