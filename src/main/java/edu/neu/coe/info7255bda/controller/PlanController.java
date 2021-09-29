package edu.neu.coe.info7255bda.controller;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jackson.JsonLoader;
import edu.neu.coe.info7255bda.constant.StatusCode;
import edu.neu.coe.info7255bda.model.VO.ResultData;
import edu.neu.coe.info7255bda.utils.exception.JsonFormatException;
import edu.neu.coe.info7255bda.utils.json.JsonValidateUtil;
import lombok.NonNull;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("plan")
public class PlanController {

    public static final String DIR_PREFIX = "./src/main/resources";
    private final static String planSchemaFilePath = DIR_PREFIX + "/json/schema/jsonSchema.json";

    @GetMapping("/hello")
    public String hello() {
        return "Hello, Plan!";
    }

    @PostMapping("/json/validate")
    public ResultData<String> validateJson(@RequestBody String strJson){
        if (JsonValidateUtil.validateJson(planSchemaFilePath, strJson)){
            return ResultData.success("No error found");
        }
        else {
            throw new JsonFormatException(StatusCode.JSON_SCHEMA_ERROR);
        }
    }

    @PostMapping("/json/findmissing")
    public List<String> findMissing(@RequestBody String strJson){
        return JsonValidateUtil.findMissingProperties(planSchemaFilePath, strJson);
    }
}