package edu.neu.coe.info7255bda.service.impl;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.neu.coe.info7255bda.constant.StatusCode;
import edu.neu.coe.info7255bda.model.VO.ResultData;
import edu.neu.coe.info7255bda.service.PlanService;
import edu.neu.coe.info7255bda.utils.exception.Customer400Exception;
import edu.neu.coe.info7255bda.utils.json.JsonUtil;
import edu.neu.coe.info7255bda.utils.json.JsonValidateUtil;
import edu.neu.coe.info7255bda.utils.redis.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

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
            if (JsonValidateUtil.isJson((String) obj)){
                return JsonValidateUtil.str2JsonNode((String) obj);
            }
            else {
                return ResultData.success(obj);
            }
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
        JsonNode jsonSchema = JsonValidateUtil.str2JsonNode(JSON.toJSONString(obj));

        if (JsonValidateUtil.isValidated(jsonSchema, strJson)){
            JsonNode jsonData = JsonValidateUtil.str2JsonNode(strJson);
            return addAsGraph(jsonData);
        }
        else {
            String result = JsonValidateUtil.validateJson(jsonSchema, strJson);
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

    @Override
    public Object getGraphByKey(String key) {
        Set<String> keys = redisUtil.getKeys(key + "*");
        if (keys == null){
            throw new Customer400Exception(StatusCode.REDIS_GET_ERROR.getCode(), StatusCode.REDIS_GET_ERROR.getMessage());
        }
        if (keys.size() == 1){
            return getPlanByKey(key);
        }
        else {
            ObjectNode objectNode = new ObjectMapper().createObjectNode();
            Map<String, JsonNode> map = new HashMap<>();
            for (String s : keys){
                if (!s.equals(key)){
                    String fieldName = s.replace(key + "_", "");
                    String edge = redisUtil.getByKey(s).toString();
                    if (edge.contains(",")){
                        ArrayNode arrayNode = new ObjectMapper().createArrayNode();
                        String[] edges = edge.split(",");
                        for (String e : edges){
                            if (hasEdge(e)){
                                arrayNode.add((ObjectNode) getGraphByKey(e));
                            }
                            else {
                                JsonNode jsonData = JsonValidateUtil.str2JsonNode(redisUtil.getByKey(e).toString());
                                arrayNode.add(jsonData);
                            }
                        }
                        objectNode.putArray(fieldName).addAll(arrayNode);
                    }
                    else {
                        JsonNode jsonData = JsonValidateUtil.str2JsonNode(redisUtil.getByKey(edge).toString());
                        map.put(fieldName, jsonData);
                    }
                }
                else {
                    JsonNode jsonData = JsonValidateUtil.str2JsonNode(redisUtil.getByKey(s).toString());
                    Iterator<String> iterator = jsonData.fieldNames();
                    while (iterator.hasNext()){
                        String fieldName = iterator.next();
                        objectNode.set(fieldName, jsonData.get(fieldName));
                    }
                }
            }
            objectNode.setAll(map);
            if (objectNode.isEmpty()){
                throw new Customer400Exception(StatusCode.REDIS_GET_ERROR.getCode(), StatusCode.REDIS_GET_ERROR.getMessage());
            }
            return objectNode;
        }
    }

    @Override
    public String delGraphByKey(String key) {
        Set<String> keys = redisUtil.getKeys(key + "*");
        if (keys == null){
            throw new Customer400Exception(StatusCode.REDIS_GET_ERROR.getCode(), StatusCode.REDIS_GET_ERROR.getMessage());
        }
        if (keys.size() == 1){
            return "No edge need to be deleted!";
        }
        else {
            for (String s : keys){
              if (!s.equals(key)){
                  delPlanByKey(s);
              }
            }
            return "Deletion success";
        }
    }

    @Override
    public String delEdgeByKey(String key) {
        if (redisUtil.delByKey(key)){
            return "Deletion success";
        }
        else {
            return "No edge can be deleted!";
        }
    }

    @Override
    public String delEdgeByKeyAndN(String key, int n) {
        String keys = redisUtil.getByKey(key).toString();
        if (keys.isEmpty()){
            return "No edge can be deleted!";
        }
        else if (!keys.contains(",")){
            return "Only one edge found!";
        }
        else {
            String[] s = keys.split(",");
            redisUtil.setKV(key, keys.replace(s[n], ""));
            return "Deletion success";
        }
    }

    @Override
    public Map<String, String> addOtherPlan(String strJson) {
        JsonNode jsonData = JsonValidateUtil.str2JsonNode(strJson);
        return addAsGraph(jsonData);
    }

    private Map<String, String> addAsGraph(JsonNode data){
        Map<String, String> map = JsonUtil.convert2Graph(data, "", "");
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

    private boolean hasEdge(String key){
        return redisUtil.getKeys(key) != null;
    }
}
