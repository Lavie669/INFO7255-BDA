package edu.neu.coe.info7255bda.utils.json;

import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.interfaces.RSAKeyProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import edu.neu.coe.info7255bda.constant.Constant;
import edu.neu.coe.info7255bda.utils.exception.Customer400Exception;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.*;

@Component
public class JsonUtil {

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

    public static JsonNode readFromFileToJson(String filePath){
        return JsonValidateUtil.str2JsonNode(readFromFile(filePath));
    }

    public static Map<String, String> convert2Graph(JsonNode jsonData, String key, String fieldName){
        checkNode(jsonData);
        Map<String, String> map = new HashMap<>();
        String ownKey = jsonData.get(Constant.OBJECT_TYPE).asText() + '_' + jsonData.get(Constant.OBJECT_ID).asText();
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
        if (!jsonNode.has(Constant.OBJECT_ID)){
            throw new Customer400Exception(400, "Missing " + Constant.OBJECT_ID);
        }
        else if (!jsonNode.has(Constant.OBJECT_TYPE)){
            throw new Customer400Exception(400, "Missing " + Constant.OBJECT_TYPE);
        }
    }

    public static void main(String[] args) throws Exception {
        String strJson = "{\n" +
                "\t\"planCostShares\": {\n" +
                "\t\t\"deductible\": 2000,\n" +
                "\t\t\"_org\": \"example.com\",\n" +
                "\t\t\"copay\": 23,\n" +
                "\t\t\"objectId\": \"1234vxc2324sdf-501\",\n" +
                "\t\t\"objectType\": \"membercostshare\"\n" +
                "\t\t\n" +
                "\t},\n" +
                "\t\"_org\": \"example.com\",\n" +
                "\t\"objectId\": \"12xvxc345ssdsds-508\",\n" +
                "\t\"objectType\": \"plan\",\n" +
                "\t\"planType\": \"inNetwork\",\n" +
                "\t\"creationDate\": \"12-12-2017\"\n" +
                "}";
        JsonNode  jsonNode = JsonValidateUtil.str2JsonNode(strJson);
        System.out.println(convert2Graph(jsonNode, "", ""));
    }
}
