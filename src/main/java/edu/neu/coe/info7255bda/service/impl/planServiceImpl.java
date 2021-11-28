package edu.neu.coe.info7255bda.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.neu.coe.info7255bda.constant.Constant;
import edu.neu.coe.info7255bda.constant.StatusCode;
import edu.neu.coe.info7255bda.model.VO.ResultData;
import edu.neu.coe.info7255bda.service.PlanService;
import edu.neu.coe.info7255bda.utils.exception.Customer304Exception;
import edu.neu.coe.info7255bda.utils.exception.Customer400Exception;
import edu.neu.coe.info7255bda.utils.json.JsonUtil;
import edu.neu.coe.info7255bda.utils.json.JsonValidateUtil;
import edu.neu.coe.info7255bda.utils.redis.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.*;

@Service("planService")
@Slf4j
public class planServiceImpl implements PlanService {

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public Map<String, String> addByJson(JsonNode jsonData) {
        if (redisUtil.setKV(jsonData.get(Constant.OBJECT_ID).asText(), jsonData)){
            Map<String, String> res = new HashMap<>();
            res.put(Constant.OBJECT_ID, jsonData.get(Constant.OBJECT_ID).asText());
            res.put(Constant.OBJECT_TYPE, jsonData.get(Constant.OBJECT_TYPE).asText());
            return res;
        }
        else {
            throw new Customer400Exception(StatusCode.REDIS_SET_ERROR.getCode(), StatusCode.REDIS_SET_ERROR.getMessage());
        }
    }

    @Override
    public String addByMap(Map<String, Object> map) {
        if (redisUtil.setKV(map.get(Constant.OBJECT_ID).toString(), map)){
            return "Successfully set key/value";
        }
        else {
            throw new Customer400Exception(StatusCode.REDIS_SET_ERROR.getCode(), StatusCode.REDIS_SET_ERROR.getMessage());
        }
    }

    @Override
    public Map<String, String> validateAndAdd(String strJson) {
        if (JsonValidateUtil.isValidated(Constant.PLAN_SCHEMA_FILE_PATH, strJson)){
            JsonNode jsonData = JsonValidateUtil.str2JsonNode(strJson);
            return addByJson(jsonData);
        }
        else {
            String result = JsonValidateUtil.validateJson(JsonValidateUtil.str2JsonNode(Constant.PLAN_SCHEMA_FILE_PATH), strJson);
            throw new Customer400Exception(StatusCode.JSON_SCHEMA_ERROR.getCode(), result);
        }
    }

    @Override
    public Object getPlanByKey(String key) {
        Object obj = redisUtil.getByKey(key);
        if (obj == null){
            throw new Customer400Exception(StatusCode.REDIS_GET_ERROR.getCode(), StatusCode.REDIS_GET_ERROR.getMessage()+ '-' + key);
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
        if (JsonValidateUtil.isValidated(Constant.PLAN_SCHEMA_FILE_PATH, strJson)){
            return "No error found";
        }
        else {
            throw new Customer400Exception(StatusCode.JSON_SCHEMA_ERROR.getCode(), StatusCode.JSON_SCHEMA_ERROR.getMessage());
        }
    }

    @Override
    public List<String> findMissAtPlan(String strJson) {
        return JsonValidateUtil.findMissingProperties(Constant.PLAN_SCHEMA_FILE_PATH, strJson);
    }

    @Override
    public JsonNode getSchema() {
        return getJsonPlanByKey(Constant.SCHEMA);
    }

    private JsonNode getJsonPlanByKey(String key) {
        return JsonValidateUtil.str2JsonNode(JSON.toJSONString(getPlanByKey(key)));

    }

    @Override
    public Map<String, String> validateAndAddAsGraph(String strJson) {
        JsonNode jsonSchema = getJsonSchemaByKey(Constant.SCHEMA);

        if (JsonValidateUtil.isValidated(jsonSchema, strJson)){
            JsonNode jsonData = JsonValidateUtil.str2JsonNode(strJson);
            String key = jsonData.get(Constant.OBJECT_TYPE).asText() + "_" + jsonData.get(Constant.OBJECT_ID).asText();
            if (redisUtil.getByKey(key)!=null){
                throw new Customer400Exception(400, "Plan already existed");
            }
            return addAsGraph(jsonData);
        }
        else {
            String result = JsonValidateUtil.validateJson(jsonSchema, strJson);
            throw new Customer400Exception(StatusCode.JSON_SCHEMA_ERROR.getCode(), result);
        }
    }

    @Override
    public String addSchema(String strSchema) {
        if (redisUtil.getByKey(Constant.SCHEMA)!=null){
            throw new Customer400Exception(400, "Plan schema already existed");
        }
        JsonNode schema = JsonValidateUtil.str2JsonNode(strSchema);
        if (redisUtil.setKV(Constant.SCHEMA, schema)){
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
                            if (!e.isEmpty()){
                                if (hasEdge(e)){
                                    arrayNode.add((ObjectNode) getGraphByKey(e));
                                }
                                else {
                                    JsonNode jsonData = JsonValidateUtil.str2JsonNode(redisUtil.getByKey(e).toString());
                                    arrayNode.add(jsonData);
                                }
                            }
                        }
                        objectNode.putArray(fieldName).addAll(arrayNode);
                    }
                    else if (edge.isEmpty()){
                        map.put(fieldName, new ObjectMapper().createObjectNode());
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
    public Object getGraphWithEtag(String key, String eTag) {
        if (!checkEtag(getPlanByKey(key), eTag)){
            throw new Customer304Exception(StatusCode.NOT_MODIFIED.getCode(), StatusCode.NOT_MODIFIED.getMessage());
        }
        return getGraphByKey(key);
    }

    @Override
    public Object getValueWithEtag(String key, String eTag) {
        Object obj = getPlanByKey(key);
        if (!checkEtag(obj, eTag)){
            throw new Customer304Exception(StatusCode.NOT_MODIFIED.getCode(), StatusCode.NOT_MODIFIED.getMessage());
        }
        return obj;
    }

    @Override
    public String delGraphByKey(String key) {
        Set<String> keys = redisUtil.getKeys(key + "*");
        if (keys.size() == 0){
            throw new Customer400Exception(StatusCode.REDIS_GET_ERROR.getCode(), StatusCode.REDIS_GET_ERROR.getMessage());
        }
        else if (keys.size() == 1){
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
        String newS = "";
        if (redisUtil.getByKey(key).toString().contains(",")){
            newS += ',';
        }
        if (redisUtil.setKV(key, newS)){
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
            while (s[n].isEmpty()){
                n += 1;
            }
            redisUtil.setKV(key, keys.replace(s[n], ""));
            return "Deletion success";
        }
    }

    @Override
    public Map<String, String> addOtherPlan(String strJson) {
        JsonNode jsonData = JsonValidateUtil.str2JsonNode(strJson);
        return addAsGraph(jsonData);
    }

    @Override
    public String updatePlan(String key, String strJson) {
        JsonNode jsonNode = JsonValidateUtil.str2JsonNode(strJson);
        Set<String> keys = redisUtil.getKeys(key + "*");
        JSONObject object =  JSON.parseObject(getPlanByKey(key).toString());
        Iterator<String> iterator = jsonNode.fieldNames();
        while (iterator.hasNext()){
            String fieldName = iterator.next();
            JsonNode node = jsonNode.get(fieldName);
            if (node.isContainerNode()){
                String edge = key + '_' + fieldName;
                if (keys.contains(edge)){
                    String edgeVal = redisUtil.getByKey(edge).toString();
                    if (node.isArray()){
                        node.forEach(n->{
                            updateGraph(edge, edgeVal, n);
                        });
                    }
                    else {
                        updateGraph(edge, edgeVal, node);
                    }
                }
                else {
                    throw new Customer400Exception(400, "Nothing can be updated");
                }
            }
            else {
                if (!fieldName.equals(Constant.OBJECT_TYPE) && !fieldName.equals(Constant.OBJECT_ID)){
                    object.put(fieldName, node.asText());
                }
            }

        }
        setGraph(key, object);
        return "Update success";
    }

    @Override
    public String updatePlanWithEtag(String key, String strJson, String eTag) {
        if (eTag!=null&&checkEtag(getPlanByKey(key), eTag)){
            return null;
        }
        return updatePlan(key, strJson);
    }

    @Override
    public String updateAllPlan(String key, String strJson) {
        JsonNode jsonSchema = getJsonSchemaByKey(Constant.SCHEMA);

        if (JsonValidateUtil.isValidated(jsonSchema, strJson)){
            JsonNode jsonData = JsonValidateUtil.str2JsonNode(strJson);
            addAsGraph(jsonData);
            return "Update success";
        }
        else {
            String result = JsonValidateUtil.validateJson(jsonSchema, strJson);
            throw new Customer400Exception(StatusCode.JSON_SCHEMA_ERROR.getCode(), result);
        }
    }

    @Override
    public String updateAllPlanWithEtag(String key, String strJson, String eTag) {
        if (eTag!=null&&checkEtag(getPlanByKey(key), eTag)){
            return null;
        }
        return updateAllPlan(key, strJson);
    }

    private void updateGraph(String edge, String edgeVal, JsonNode newData){
        checkNode(newData);
        String ownKey = newData.get(Constant.OBJECT_TYPE).asText() + '_' + newData.get(Constant.OBJECT_ID).asText();
        if (!edgeVal.contains(ownKey)) {
            if (edgeVal.contains(",")){
                setGraph(edge, edgeVal + ',' + ownKey);
            }
            else {
                setGraph(edge, ownKey);
            }
        }
        JsonUtil.convert2Graph(newData, "", "").forEach(this::setGraph);
    }

    private void setGraph(String k, Object v){
        if (!redisUtil.setKV(k, v.toString())){
            throw new Customer400Exception(StatusCode.REDIS_SET_ERROR.getCode(), StatusCode.REDIS_SET_ERROR.getMessage());
        }
    }

    private Map<String, String> addAsGraph(JsonNode data){
        Map<String, String> map = JsonUtil.convert2Graph(data, "", "");
        map.forEach(this::setGraph);
        Map<String, String> res = new HashMap<>();
        res.put(Constant.OBJECT_ID, data.get(Constant.OBJECT_ID).asText());
        res.put(Constant.OBJECT_TYPE, data.get(Constant.OBJECT_TYPE).asText());
        return res;
    }

    private boolean hasEdge(String key){
        return redisUtil.getKeys(key) != null;
    }

    private boolean checkEtag(Object obj, String eTag){
        if (eTag != null){
            JsonNode json = JsonValidateUtil.str2JsonNode(obj.toString());
            if (json.has("creationDate")){
                String token = json.get("creationDate").asText();
                if (eTag.equals(DigestUtils.md5DigestAsHex(token.getBytes()))){
                    return false;
                }
                return true;
            }
        }
        return true;
    }

    private void checkNode(JsonNode jsonNode){
        if (!jsonNode.has(Constant.OBJECT_ID)){
            throw new Customer400Exception(400, "Missing " + Constant.OBJECT_ID);
        }
        else if (!jsonNode.has(Constant.OBJECT_TYPE)){
            throw new Customer400Exception(400, "Missing " + Constant.OBJECT_TYPE);
        }
    }

    private JsonNode getJsonSchemaByKey(String key){
        Object obj = redisUtil.getByKey(key);
        if (obj == null){
            throw new NullPointerException("Can't find json schema!");
        }
        return JsonValidateUtil.str2JsonNode(JSON.toJSONString(obj));
    }
}
