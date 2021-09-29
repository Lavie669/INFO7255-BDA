package edu.neu.coe.info7255bda.utils.exception;

import edu.neu.coe.info7255bda.constant.StatusCode;

public class JsonFormatException extends RuntimeException{
    private StatusCode sc;

    public JsonFormatException() {super();}

    public JsonFormatException(StatusCode sc) {
        super(sc.getMessage());
        this.sc = sc;
    }

    public int getCode(){
        return this.sc.getCode();
    }
}
