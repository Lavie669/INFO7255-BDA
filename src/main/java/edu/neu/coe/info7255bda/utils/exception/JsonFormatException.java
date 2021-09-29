package edu.neu.coe.info7255bda.utils.exception;

public class JsonFormatException extends RuntimeException{
    private final int code;

    public JsonFormatException(int code, String msg) {
        super(msg);
        this.code = code;
    }

    public int getCode(){
        return code;
    }
}
