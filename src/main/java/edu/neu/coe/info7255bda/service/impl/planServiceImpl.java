package edu.neu.coe.info7255bda.service.impl;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.JsonNode;
import edu.neu.coe.info7255bda.constant.StatusCode;
import edu.neu.coe.info7255bda.model.VO.ResultData;
import edu.neu.coe.info7255bda.service.PlanService;
import edu.neu.coe.info7255bda.utils.exception.Customer400Exception;
import edu.neu.coe.info7255bda.utils.json.JsonUtil;
import edu.neu.coe.info7255bda.utils.json.JsonValidateUtil;
import edu.neu.coe.info7255bda.utils.redis.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("planService")
public class planServiceImpl implements PlanService {

    public static final String DIR_PREFIX = "./src/main/resources";
    private final static String planSchemaFilePath = DIR_PREFIX + "/json/schema/PlanSchema.json";
    private final static String key = "objectId";
    private final static String objectType = "objectType";
    private final static String SCHEMA = "planSchema";

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public Map<String, String> addByJson(JsonNode jsonData) {
        if (redisUtil.setKV(jsonData.get(key).asText(), jsonData)){
            Map<String, String> res = new HashMap<>();
            res.put(key, jsonData.get(key).asText());
            res.put(objectType, jsonData.get(objectType).asText());
            return res;
        }
        else {
            throw new Customer400Exception(StatusCode.REDIS_SET_ERROR.getCode(), StatusCode.REDIS_SET_ERROR.getMessage());
        }
    }

    @Override
    public String addByMap(Map<String, Object> map) {
        if (redisUtil.setKV(map.get(key).toString(), map)){
            return "Successfully set key/value";
        }
        else {
            throw new Customer400Exception(StatusCode.REDIS_SET_ERROR.getCode(), StatusCode.REDIS_SET_ERROR.getMessage());
        }
    }

    @Override
    public Map<String, String> validateAndAdd(String strJson) {
        if (JsonValidateUtil.isValidated(planSchemaFilePath, strJson)){
            JsonNode jsonData = JsonValidateUtil.str2JsonNode(strJson);
            return addByJson(jsonData);
        }
        else {
            String result = JsonValidateUtil.validateJson(JsonValidateUtil.str2JsonNode(planSchemaFilePath), strJson);
            throw new Customer400Exception(StatusCode.JSON_SCHEMA_ERROR.getCode(), result);
        }
    }

    @Override
    public Object getPlanByKey(String key) {
        Object obj = redisUtil.getByKey(key);
        if (obj == null){
            throw new Customer400Exception(StatusCode.REDIS_GET_ERROR.getCode(), StatusCode.REDIS_GET_ERROR.getMessage());
        }
        if (obj instanceof String){
            return ResultData.success(obj);
        }
        return obj;
    }

    @Override
    public String delPlanByKey(String key) {
        if (redisUtil.delByKey(key)){
            return "Deletion success";
        }
        else {
            throw new Customer400Exception(StatusCode.REDIS_DEL_ERROR.getCode(), StatusCode.REDIS_DEL_ERROR.getMessage());
        }
    }

    @Override
    public String validatePlan(String strJson) {
        if (JsonValidateUtil.isValidated(planSchemaFilePath, strJson)){
            return "No error found";
        }
        else {
            throw new Customer400Exception(StatusCode.JSON_SCHEMA_ERROR.getCode(), StatusCode.JSON_SCHEMA_ERROR.getMessage());
        }
    }

    @Override
    public List<String> findMissAtPlan(String strJson) {
        return JsonValidateUtil.findMissingProperties(planSchemaFilePath, strJson);
    }

    @Override
    public JsonNode getJsonPlanByKey(String key) {
        return JsonValidateUtil.str2JsonNode(JSON.toJSONString(getPlanByKey(key)));

    }

    @Override
    public Map<String, String> validateAndAddAsGraph(String strJson) {
        Object obj = redisUtil.getByKey(SCHEMA);
        if (obj == null){
            throw new NullPointerException("Can't find json schema!");
        }
        JsonNode jsonNode = JsonValidateUtil.str2JsonNode(JSON.toJSONString(obj));

        if (JsonValidateUtil.isValidated(jsonNode, strJson)){
            JsonNode jsonData = JsonValidateUtil.str2JsonNode(strJson);
            return addAsGraph(jsonData);
        }
        else {
            String result = JsonValidateUtil.validateJson(jsonNode, strJson);
            throw new Customer400Exception(StatusCode.JSON_SCHEMA_ERROR.getCode(), result);
        }
    }

    @Override
    public String addSchema(String strSchema) {
        JsonNode schema = JsonValidateUtil.str2JsonNode(strSchema);
        if (redisUtil.setKV(SCHEMA, schema)){
            return "Successfully set plan schema!";
        }
        else {
            throw new Customer400Exception(StatusCode.REDIS_SET_ERROR.getCode(), StatusCode.REDIS_SET_ERROR.getMessage());
        }
    }

    private Map<String, String> addAsGraph(JsonNode data){
        Map<String, String> map = JsonUtil.convert2Graph(data, "");
        map.forEach((k, v)->{
            if (!redisUtil.setKV(k, v)){
                throw new Customer400Exception(StatusCode.REDIS_SET_ERROR.getCode(), StatusCode.REDIS_SET_ERROR.getMessage());
            }
        });
        Map<String, String> res = new HashMap<>();
        res.put(key, data.get(key).asText());
        res.put(objectType, data.get(objectType).asText());
        return res;
    }
}
