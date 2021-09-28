package edu.neu.coe.info7255bda.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jackson.JsonNodeReader;
import com.github.fge.jsonschema.core.report.LogLevel;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import org.springframework.util.ResourceUtils;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class JsonValidateUtil {
    private final static JsonSchemaFactory factory = JsonSchemaFactory.byDefault();

    private static JsonNode strToJsonNode(String jsonStr) {
        JsonNode jsonNode = null;
        try {
            jsonNode = JsonLoader.fromString(jsonStr);
        } catch (IOException e) {
            e.printStackTrace();
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

    public static boolean validateJson(String schemaFilePath, String jsonStr){
        return getReport(schemaFilePath, jsonStr).isSuccess();
    }

    public static List<String> findMissingProperties(String schemaFilePath, String jsonStr){
        ProcessingReport report = getReport(schemaFilePath, jsonStr);
        Iterator<ProcessingMessage> it = report.iterator();
        StringBuilder sb = new StringBuilder();
        List<String> missingP = new ArrayList<>();
        sb.append("Json format error: ");
        while (it.hasNext()){
            ProcessingMessage pm = it.next();
            if (!LogLevel.WARNING.equals(pm.getLogLevel())) {
                JsonNode node = pm.asJson().get("missing");
                for (JsonNode n : node){
                    missingP.add(n.asText());
                }
                sb.append(pm);
            }
        }
        System.out.println(sb);
        return missingP;
    }
}
