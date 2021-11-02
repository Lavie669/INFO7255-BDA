package edu.neu.coe.info7255bda.utils.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.neu.coe.info7255bda.utils.exception.Customer400Exception;
import edu.neu.coe.info7255bda.utils.redis.RedisUtil;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class JsonUtil {

    public static final String DIR_PREFIX = "./src/main/resources";
    private final static String testJson1 = DIR_PREFIX + "/json/testPlanJson1.json";
    private final static String testJson2 = DIR_PREFIX + "/json/testPlanJson2.json";
    private final static String OBJECT_ID = "objectId";
    private final static String OBJECT_TYPE = "objectType";

    public static String readFromFile(String filePath){
        StringBuilder sb = new StringBuilder();
        try{
            InputStream in = new FileInputStream(filePath);
            InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(reader);
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append("\n").append(line);
            }
            reader.close();
        } catch (IOException e){
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static Map<String, String> convert2Graph(JsonNode jsonData, String key, String fieldName){
        checkNode(jsonData);
        Map<String, String> map = new HashMap<>();
        String ownKey = jsonData.get(OBJECT_TYPE).asText() + '_' + jsonData.get(OBJECT_ID).asText();
        if (!key.isEmpty()&&!fieldName.isEmpty()){
            map.put(key+'_'+fieldName, ownKey);
        }
        ObjectNode objectNode = new ObjectMapper().createObjectNode();
        Iterator<String> iterator = jsonData.fieldNames();
        while (iterator.hasNext()){
            String field = iterator.next();
            JsonNode node = jsonData.get(field);
            if (node.isContainerNode()){
                if (node.isArray()){
                    for (JsonNode n : node){
                        Map<String, String> m = convert2Graph(n, ownKey, field);
                        m.forEach((k ,v) ->{
                            if (map.containsKey(k)){
                                map.put(k, map.get(k) + ',' + v);
                            }
                            else {
                                map.put(k, v);
                            }
                        });
                    }
                }
                else {
                    map.putAll(convert2Graph(node, ownKey, field));
                }
            }
            else {
                objectNode.set(field, node);
            }
        }
        map.put(ownKey, objectNode.toString());
        return map;
    }

    private static void checkNode(JsonNode jsonNode){
        if (!jsonNode.has(OBJECT_ID)){
            throw new Customer400Exception(400, "Missing " + OBJECT_ID);
        }
        else if (!jsonNode.has(OBJECT_TYPE)){
            throw new Customer400Exception(400, "Missing " + OBJECT_TYPE);
        }
    }

    public static void main(String[] args){
        String edge = ",123";
        System.out.println(edge.contains(",123"));
    }
}
