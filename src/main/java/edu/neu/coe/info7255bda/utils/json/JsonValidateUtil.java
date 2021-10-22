package edu.neu.coe.info7255bda.utils.json;


import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jackson.JsonNodeReader;
import com.github.fge.jsonschema.core.report.LogLevel;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import edu.neu.coe.info7255bda.constant.StatusCode;
import edu.neu.coe.info7255bda.utils.exception.Customer400Exception;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
@Component
public class JsonValidateUtil {
    private final static JsonSchemaFactory factory = JsonSchemaFactory.byDefault();

    public static JsonNode str2JsonNode(String jsonStr) {
        JsonNode jsonNode = null;
        try {
            jsonNode = JsonLoader.fromString(jsonStr);
        } catch (IOException e) {
            throw new Customer400Exception(StatusCode.JSON_FORMAT_ERROR.getCode(), StatusCode.JSON_FORMAT_ERROR.getMessage());
        }
        return jsonNode;
    }

    private static JsonNode schema2JsonNode(String schemaFilePath){
        JsonNode jsonSchemaNode = null;
        try{
            jsonSchemaNode = new JsonNodeReader().fromReader(new FileReader(ResourceUtils.getFile(schemaFilePath)));
        } catch (IOException e){
            e.printStackTrace();
        }
        return jsonSchemaNode;
    }

    public static ProcessingReport getReport(String schemaFilePath, String jsonStr){
        ProcessingReport report;
        JsonNode jsonSchema = schema2JsonNode(schemaFilePath);
        JsonNode jsonData = str2JsonNode(jsonStr);
        report = factory.getValidator().validateUnchecked(jsonSchema, jsonData);
        return report;
    }

    public static ProcessingReport getReport(JsonNode jsonSchema, String jsonStr){
        ProcessingReport report;
        JsonNode jsonData = str2JsonNode(jsonStr);
        report = factory.getValidator().validateUnchecked(jsonSchema, jsonData);
        return report;
    }

    public static String validateJson(JsonNode jsonSchema,String jsonStr){
        ProcessingReport report = getReport(jsonSchema, jsonStr);
        Iterator<ProcessingMessage> it = report.iterator();
        StringBuilder sb = new StringBuilder();
        while (it.hasNext()){
            ProcessingMessage pm = it.next();
            if (!LogLevel.WARNING.equals(pm.getLogLevel())) {
                System.out.println(pm);
                JsonNode jsonData = pm.asJson();
                if (jsonData.get("keyword").asText().equals("required")){
                    String position = jsonData.get("instance").get("pointer").asText();
                    String miss = jsonData.get("missing").toString();
                    sb.append("Missing required properties: ").append(miss, 1, miss.length()-1);
                    if (!position.isEmpty()){
                        sb.append(" at ").append(position);
                    }
                }
                else {
                    String position = jsonData.get("instance").get("pointer").asText();
                    String expected = jsonData.get("expected").toString();
                    String found = jsonData.get("found").asText();
                    sb.append("expected-").append(expected, 2, expected.length()-2).append(" ").append("found-").append(found).append(" at ").append(position);
                }
            }
        }
        if (!sb.isEmpty()){
            log.info("Json schema error: " + sb);
        }
        return sb.toString();
    }

    public static boolean isValidated(String schemaFilePath, String jsonStr){
        return getReport(schemaFilePath, jsonStr).isSuccess();
    }

    public static boolean isValidated(JsonNode jsonSchema, String jsonStr){
        return getReport(jsonSchema, jsonStr).isSuccess();
    }

    public static List<String> findMissingProperties(String schemaFilePath,String jsonStr){
        ProcessingReport report = getReport(schemaFilePath, jsonStr);
        Iterator<ProcessingMessage> it = report.iterator();
        List<String> missingP = new ArrayList<>();
        while (it.hasNext()){
            ProcessingMessage pm = it.next();
            if (!LogLevel.WARNING.equals(pm.getLogLevel())) {
                JsonNode node = pm.asJson().get("missing");
                if (node == null){
                    throw new Customer400Exception(StatusCode.JSON_SCHEMA_ERROR.getCode(), "Something wrong with the " + pm.asJson().get("keyword").asText());
                }
                else {
                    for (JsonNode n : node){
                        missingP.add(n.asText());
                    }
                }
            }
        }
        if (!missingP.isEmpty()){
            log.info("Json has missing required properties: " + missingP);
        }
        return missingP;
    }
}
