package edu.neu.coe.info7255bda.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import edu.neu.coe.info7255bda.constant.StatusCode;
import edu.neu.coe.info7255bda.model.VO.ResultData;
import edu.neu.coe.info7255bda.service.PlanService;
import edu.neu.coe.info7255bda.utils.exception.CustomerException;
import edu.neu.coe.info7255bda.utils.json.JsonValidateUtil;
import edu.neu.coe.info7255bda.utils.redis.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service("planService")
public class planServiceImpl implements PlanService {

    public static final String DIR_PREFIX = "./src/main/resources";
    private final static String planSchemaFilePath = DIR_PREFIX + "/json/schema/jsonSchema.json";
    private final static String key = "objectId";
    private final static String objectType = "objectType";

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
            throw new CustomerException(StatusCode.REDIS_SET_ERROR.getCode(), StatusCode.REDIS_SET_ERROR.getMessage());
        }
    }

    @Override
    public String addByMap(Map<String, Object> map) {
        if (redisUtil.setKV(map.get(key).toString(), map)){
            return "Successfully set key/value";
        }
        else {
            throw new CustomerException(StatusCode.REDIS_SET_ERROR.getCode(), StatusCode.REDIS_SET_ERROR.getMessage());
        }
    }

    @Override
    public Map<String, String> validateAndAdd(String strJson) {
        if (JsonValidateUtil.isValidated(planSchemaFilePath, strJson)){
            JsonNode jsonData = JsonValidateUtil.strToJsonNode(strJson);
            return addByJson(jsonData);
        }
        else {
            String result = JsonValidateUtil.validateJson(planSchemaFilePath, strJson);
            throw new CustomerException(StatusCode.JSON_SCHEMA_ERROR.getCode(), result);
        }
    }

    @Override
    public Object getPlanByKey(String key) {
        return null;
    }
}
