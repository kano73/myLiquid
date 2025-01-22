package org.example.exeptions;

public class Md5SumNotMatch extends RuntimeException {
    public Md5SumNotMatch(String message) {
        super(message);
    }
}
