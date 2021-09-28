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
import java.util.Iterator;

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

    public static boolean validateJson(String schemaFilePath, String jsonStr){
        ProcessingReport report;
        JsonNode jsonSchema = schemaToJsonNode(schemaFilePath);
        JsonNode jsonData = strToJsonNode(jsonStr);
        report = factory.getValidator().validateUnchecked(jsonSchema, jsonData);

        if (report.isSuccess()){
            return true;
        }
        else {
            Iterator<ProcessingMessage> it = report.iterator();
            StringBuilder sb = new StringBuilder();
            sb.append("Json format error: ");
            while (it.hasNext()){
                ProcessingMessage pm = it.next();
                if (!LogLevel.WARNING.equals(pm.getLogLevel())) {
                    sb.append(pm);
                }
            }
            System.out.println(sb);
            return false;
        }

    }
}
