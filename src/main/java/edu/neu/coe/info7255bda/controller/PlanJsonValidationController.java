package edu.neu.coe.info7255bda.controller;

import edu.neu.coe.info7255bda.utils.JsonValidateUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("plan/json")
public class PlanJsonValidationController {

    private final static String planSchemaFilePath = "/json/schema/jsonSchema.json";

    @PostMapping("validate")
    public String validateJson(@RequestBody String strJson){
        if (JsonValidateUtil.validateJson(planSchemaFilePath, strJson)){
            return "No error found.";
        }
        else {
            return "Json format error!!!";
        }
    }
}
