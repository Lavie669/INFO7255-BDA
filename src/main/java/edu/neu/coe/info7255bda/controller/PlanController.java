package edu.neu.coe.info7255bda.controller;

import com.fasterxml.jackson.databind.JsonNode;
import edu.neu.coe.info7255bda.constant.StatusCode;
import edu.neu.coe.info7255bda.model.VO.ResultData;
import edu.neu.coe.info7255bda.utils.exception.CustomerException;
import edu.neu.coe.info7255bda.utils.json.JsonValidateUtil;
import edu.neu.coe.info7255bda.utils.redis.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("plan")
public class PlanController {

    public static final String DIR_PREFIX = "./src/main/resources";
    private final static String planSchemaFilePath = DIR_PREFIX + "/json/schema/jsonSchema.json";
    private final static String objectID = "objectId";
    private final static String objectType = "objectType";

    @Autowired
    private RedisUtil redisUtil;

    @GetMapping("/hello")
    public String hello() {
        return "Hello, Plan!";
    }

    @PostMapping("/add")
    public Map<String, String> addPlan(@RequestBody String strJson){
        if (JsonValidateUtil.isValidated(planSchemaFilePath, strJson)){
            JsonNode jsonData = JsonValidateUtil.strToJsonNode(strJson);
            Map<String, String> map = new HashMap<>();
            map.put(objectID, jsonData.get(objectID).asText());
            map.put(objectType, jsonData.get(objectType).asText());
            return map;
        }
        else {
            String result = JsonValidateUtil.validateJson(planSchemaFilePath, strJson);
            throw new CustomerException(StatusCode.JSON_SCHEMA_ERROR.getCode(), result);
        }
    }

    @PostMapping("redis/add")
    public ResultData<String> setKeyValue(@RequestBody Map<String, Object> paramsMap){
        if (redisUtil.setKV(paramsMap.get("name").toString(), paramsMap)){
            return ResultData.success("Successfully set key/value");
        }
        else {
            return ResultData.fail(StatusCode.REDIS_SET_ERROR.getCode(), StatusCode.PARAMETER_ERROR.getMessage());
        }
    }

    @GetMapping("redis/getKeys")
    public Set<String> getKeysByPrefix(@RequestBody String prefix){
        return redisUtil.getKeys(prefix);
    }

    @GetMapping("redis/getValue")
    public Object getValueByKey(@RequestBody String key){
        return redisUtil.getByKey(key);
    }

    @DeleteMapping("redis/del")
    public ResultData<String> delByKey(@RequestBody String key){
        if (redisUtil.delByKey(key)){
            return ResultData.success("Deletion success");
        }
        else {
            throw new CustomerException(StatusCode.REDIS_DEL_ERROR.getCode(), StatusCode.REDIS_DEL_ERROR.getMessage());
        }
    }

    @PostMapping("/json/validate")
    public ResultData<String> validate(@RequestBody String strJson){
        if (JsonValidateUtil.isValidated(planSchemaFilePath, strJson)){
            return ResultData.success("No error found");
        }
        else {
            throw new CustomerException(StatusCode.JSON_SCHEMA_ERROR.getCode(), StatusCode.JSON_SCHEMA_ERROR.getMessage());
        }
    }

    @PostMapping("/json/findmissing")
    public List<String> findMissing(@RequestBody String strJson){
        return JsonValidateUtil.findMissingProperties(planSchemaFilePath, strJson);
    }
}