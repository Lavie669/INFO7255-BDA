package edu.neu.coe.info7255bda.model.VO;

import edu.neu.coe.info7255bda.constant.StatusCode;
import lombok.Data;

@Data
public class ResultData<T> {
    private int statusCode;
    private String message;
    private T data;
    private long timestamp ;

    public ResultData (){
        this.timestamp = System.currentTimeMillis();
    }


    public static <T> ResultData<T> success(T data) {
        ResultData<T> resultData = new ResultData<>();
        resultData.setStatusCode(StatusCode.SUCCESS.getCode());
        resultData.setMessage(StatusCode.SUCCESS.getMessage());
        resultData.setData(data);
        return resultData;
    }

    public static <T> ResultData<T> fail(int code, String message) {
        ResultData<T> resultData = new ResultData<>();
        resultData.setStatusCode(code);
        resultData.setMessage(message);
        return resultData;
    }
}
