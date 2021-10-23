package edu.neu.coe.info7255bda.utils.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class JsonUtil {

    public static final String DIR_PREFIX = "./src/main/resources";
    private final static String testJson = DIR_PREFIX + "/json/testPlanJson1.json";
    private final static String objectId = "objectId";
    private final static String objectType = "objectType";

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
        Map<String, String> map = new HashMap<>();
        String ownKey = jsonData.get(objectType).asText() + '_' + jsonData.get(objectId).asText();
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

    public static void main(String[] args){
        String s1 = "planservice_27283xvx9asdff-504";
        String s2 = "planservice_27283xvx9asdff-504_linkedService";
        String s3 = s2.replace(s1+"_", "");
        System.out.println(s3);
    }
}
