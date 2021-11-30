package edu.neu.coe.info7255bda.constant;


public enum StatusCode {
    SUCCESS(200, "Success"),
    NOT_MODIFIED(304, "Not Modified"),
    JSON_SCHEMA_ERROR(400, "Json schema error"),
    JSON_FORMAT_ERROR(400, "Json format error"),
    PARAMETER_BODY_ERROR(400, "Request body can not be empty"),
    PARAMETER_TYPE_ERROR(400, "Request parameter type error"),
    REQUEST_METHOD_ERROR(405, "Request method not supported"),
    REDIS_GET_ERROR(400, "Can not find anything by this key"),
    REDIS_SET_ERROR(400, "Key/value store error"),
    REDIS_DEL_ERROR(400, "Can not delete by this key"),
    UNKNOWN_ERROR(500, "Unknown error, please contact the technical staff");

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
