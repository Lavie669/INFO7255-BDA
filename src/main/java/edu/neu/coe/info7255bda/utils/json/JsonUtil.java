package edu.neu.coe.info7255bda.utils.json;

import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.interfaces.RSAKeyProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
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

    public static JsonNode readFromFileToJson(String filePath){
        return JsonValidateUtil.str2JsonNode(readFromFile(filePath));
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

    public static void main(String[] args) throws Exception {
        String token = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjI3YzcyNjE5ZDA5MzVhMjkwYzQxYzNmMDEwMTY3MTM4Njg1ZjdlNTMiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiIxMDI0MTA3ODM5NTEwLTJqNTd1MDRnazMzYWQ0bHJvMDFnaDk0b245N2NvZ3A4LmFwcHMuZ29vZ2xldXNlcmNvbnRlbnQuY29tIiwiYXVkIjoiMTAyNDEwNzgzOTUxMC0yajU3dTA0Z2szM2FkNGxybzAxZ2g5NG9uOTdjb2dwOC5hcHBzLmdvb2dsZXVzZXJjb250ZW50LmNvbSIsInN1YiI6IjExMzE3NzQ2MzQ4MjkzNDcyMjY3NCIsImF0X2hhc2giOiJoeEtuc3hhQ2ppajkxLUJTamx4Q1F3IiwiaWF0IjoxNjM2MTc5NDY4LCJleHAiOjE2MzYxODMwNjh9.FmpZDrGRCv2rmZFRlymq4XSEwmleaugl8HcjmISCo5HfJvBhYwwZhhw8pxYStDUb8lGlAhv0k5y1qHuZdiEGHIXZr4jZtF8EAslbU_0jWlAMc09BHWrm8mTu0V3VWTgBcT5JYDGZezJaO7G4-B7RGcCtEEPjrlKcQcGWvVHfjkhuP1YLBYTqmmPtixzZg6YwTOYE65stCFXLyuUbJAHQnEQky2zMloFAMGra7p7CPIe9I9VKJ-M8yY3aPJZCycqT9ccRSRJp2QZ5lR7pY05v7LnKSQiizRar5MPx6tZX_A8g8qlxm-OO1uw-1_sSmSNZYuxP54OZEbrOqfnCNTrGKg";
        JsonNode jsonNode = JsonValidateUtil.str2JsonNode(readFromFile(DIR_PREFIX+"/public.crt"));
        String pk = jsonNode.get("27c72619d0935a290c41c3f010167138685f7e53").asText();
        InputStream in = new ByteArrayInputStream(pk.getBytes());
        CertificateFactory f = CertificateFactory.getInstance("X.509");
        X509Certificate certificate = (X509Certificate)f.generateCertificate(in);
        PublicKey publicKey = certificate.getPublicKey();
        NimbusJwtDecoder otherJwtDecoder = NimbusJwtDecoder.withPublicKey((RSAPublicKey) publicKey).build();
        System.out.println(otherJwtDecoder.decode(token));
    }
}
