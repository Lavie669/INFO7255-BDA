package edu.neu.coe.info7255bda.controller;

import edu.neu.coe.info7255bda.utils.JsonValidateUtil;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public String validateJson(@RequestBody String strJson){
        if (JsonValidateUtil.validateJson(planSchemaFilePath, strJson)){
            return "No error found.";
        }
        else {
            return "Json format error!!!";
        }
    }

    @PostMapping("/json/findmissing")
    public String findMissing(@RequestBody String strJson){
        List<String> result = JsonValidateUtil.findMissingProperties(planSchemaFilePath, strJson);
        if (result.isEmpty()){
            return "Nothing is missing.";
        }
        return "Missing: " + result;
    }
}