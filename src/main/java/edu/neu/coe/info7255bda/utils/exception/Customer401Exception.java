package edu.neu.coe.info7255bda.utils.exception;

public class Customer401Exception extends RuntimeException{
    private final int code;

    public Customer401Exception(String msg) {
        super(msg);
        this.code = 401;
    }

    public int getCode(){
        return code;
    }
}

