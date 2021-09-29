package edu.neu.coe.info7255bda.constant;


public enum StatusCode {
    SUCCESS(200, "Success"),
    PARAMETER_ERROR(400, "Request parameter error"),
    JSON_SCHEMA_ERROR(400, "Json schema error"),
    JSON_FORMAT_ERROR(400, "Json format error"),
    PARAMETER_BODY_ERROR(400, "Request body can not be empty"),
    REQUEST_METHOD_ERROR(405, "Request method not supported"),
    REDIS_GET_ERROR(400, "Can not find anything by this key"),
    REDIS_SET_ERROR(400, "Key/value store error"),
    REDIS_DEL_ERROR(400, "Can not delete by this key"),
    UNKNOWN_ERROR(999, "Unknown error");

    private final int code;

    private final String message;

    StatusCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
