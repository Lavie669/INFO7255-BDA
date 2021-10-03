package edu.neu.coe.info7255bda.utils.exception;

public class Customer304Exception extends RuntimeException{
    private final int code;

    public Customer304Exception(int code, String msg) {
        super(msg);
        this.code = code;
    }

    public int getCode(){
        return code;
    }
}

