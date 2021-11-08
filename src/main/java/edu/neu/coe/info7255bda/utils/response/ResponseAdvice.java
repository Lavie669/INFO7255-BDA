package edu.neu.coe.info7255bda.utils.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.neu.coe.info7255bda.model.VO.ResultData;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class ResponseAdvice implements ResponseBodyAdvice<Object> {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @SneakyThrows
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        if(body instanceof String){
            return objectMapper.writeValueAsString(ResultData.success(body));
        }
        if (body instanceof ResultData){
            return body;
        }
        if (body.toString().contains("status=404, error=Not Found")){
            return ResultData.fail(404, "Not Found");
        }
        else if (body.toString().contains("status=401, error=Unauthorized")){
            return ResultData.fail(401, "Unauthorized");
        }
        else if (body.toString().contains("status=412, error=Precondition Failed")){
            return ResultData.fail(412, "Precondition Failed");
        }
        return ResultData.success(body);
    }
}
