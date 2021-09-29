package edu.neu.coe.info7255bda.constant;

public enum StatusCode {
    SUCCESS(200, "Success"),
    PARAMETER_ERROR(400, "Request parameter error"),
    JSON_SCHEMA_ERROR(400, "Json schema error"),
    JSON_FORMAT_ERROR(400, "Json format error"),
    PARAMETER_BODY_ERROR(400, "Request body can not be empty"),
    REQUEST_METHOD_ERROR(405, "Request method not supported");

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
