package edu.neu.coe.info7255bda.utils.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import com.sun.mail.util.BASE64EncoderStream;
import edu.neu.coe.info7255bda.utils.exception.Customer400Exception;
import edu.neu.coe.info7255bda.utils.redis.RedisUtil;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
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

    public static void main(String[] args) throws Exception {
        String pkS = readFromFile(DIR_PREFIX+"/key.public").replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "").replace("\n", "");
        String token = "eyJhbGciOiJSUzI1NiIsImtpZCI6Ijg1ODI4YzU5Mjg0YTY5YjU0YjI3NDgzZTQ4N2MzYmQ0NmNkMmEyYjMiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiIxMDI0MTA3ODM5NTEwLTJqNTd1MDRnazMzYWQ0bHJvMDFnaDk0b245N2NvZ3A4LmFwcHMuZ29vZ2xldXNlcmNvbnRlbnQuY29tIiwiYXVkIjoiMTAyNDEwNzgzOTUxMC0yajU3dTA0Z2szM2FkNGxybzAxZ2g5NG9uOTdjb2dwOC5hcHBzLmdvb2dsZXVzZXJjb250ZW50LmNvbSIsInN1YiI6IjExMzE3NzQ2MzQ4MjkzNDcyMjY3NCIsImF0X2hhc2giOiJXYk5OelFhbkw4cDY3dG5ucV90TVFBIiwiaWF0IjoxNjM1OTU2NjE1LCJleHAiOjE2MzU5NjAyMTV9.UxpfYK0Mpi7b62pC9BEoCV3C07yzG6GEjBB5R35m6y2fGMBpzasU8ACcB7myNpESjWv2x_v_4SkXhEdZV7lRuZuHBhoeGwTgpbzzU4kR05WbglQhCz2AUqjP_ZHe7H_ShiQxtBMrY1nUNzQEgmG490Gb1NLaMVJp3ZFMSLgh6r58ANrK2m-gxWQ6dPLdCzkoHBghi-LAU6iQM0ZtZtuubFYVSsUInlQ1E0GVu1bKFmtDde_-35v2Uz7TK_1hNrRwUCFDCOB1IFZ-LJlnInVc7DPrPRdhAkcVHDgpNgFZumEpu6-bRGUb0sgoUAqLmd8Y0zREsj1Pv1eydsG8u87c8Q";
        SignedJWT sjwt = SignedJWT.parse(token);
        System.out.println(Arrays.toString(sjwt.getParsedParts()));
    }
}
