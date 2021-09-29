package edu.neu.coe.info7255bda.utils.json;


import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jackson.JsonNodeReader;
import com.github.fge.jsonschema.core.report.LogLevel;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import edu.neu.coe.info7255bda.constant.StatusCode;
import edu.neu.coe.info7255bda.utils.exception.JsonFormatException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ResourceUtils;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
public class JsonValidateUtil {
    private final static JsonSchemaFactory factory = JsonSchemaFactory.byDefault();

    public static JsonNode strToJsonNode(String jsonStr) {
        JsonNode jsonNode = null;
        try {
            jsonNode = JsonLoader.fromString(jsonStr);
        } catch (IOException e) {
            throw new JsonFormatException(StatusCode.JSON_FORMAT_ERROR.getCode(), StatusCode.JSON_FORMAT_ERROR.getMessage());
        }
        return jsonNode;
    }

    private static JsonNode schemaToJsonNode(String schemaFilePath){
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
        JsonNode jsonSchema = schemaToJsonNode(schemaFilePath);
        JsonNode jsonData = strToJsonNode(jsonStr);
        report = factory.getValidator().validateUnchecked(jsonSchema, jsonData);
        return report;
    }

    public static String validateJson(String schemaFilePath,String jsonStr){
        ProcessingReport report = getReport(schemaFilePath, jsonStr);
        Iterator<ProcessingMessage> it = report.iterator();
        StringBuilder sb = new StringBuilder();
        while (it.hasNext()){
            ProcessingMessage pm = it.next();
            if (!LogLevel.WARNING.equals(pm.getLogLevel())) {
                System.out.println(pm);
                JsonNode jsonData = pm.asJson();
                if (jsonData.get("keyword").asText().equals("required")){
                    sb.append(pm.getMessage());
                }
                else {
                    String position = jsonData.get("instance").get("pointer").asText().substring(1);
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

    public static List<String> findMissingProperties(String schemaFilePath,String jsonStr){
        ProcessingReport report = getReport(schemaFilePath, jsonStr);
        Iterator<ProcessingMessage> it = report.iterator();
        List<String> missingP = new ArrayList<>();
        while (it.hasNext()){
            ProcessingMessage pm = it.next();
            if (!LogLevel.WARNING.equals(pm.getLogLevel())) {
                JsonNode node = pm.asJson().get("missing");
                for (JsonNode n : node){
                    missingP.add(n.asText());
                }
            }
        }
        if (!missingP.isEmpty()){
            log.info("Json has missing required properties: " + missingP);
        }
        return missingP;
    }
}
