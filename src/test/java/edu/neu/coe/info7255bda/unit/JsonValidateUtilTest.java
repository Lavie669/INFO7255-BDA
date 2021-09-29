package edu.neu.coe.info7255bda.unit;

import static org.junit.Assert.*;

import edu.neu.coe.info7255bda.utils.json.JsonFileUtil;
import edu.neu.coe.info7255bda.utils.json.JsonValidateUtil;
import org.junit.jupiter.api.Test;

public class JsonValidateUtilTest {
    public static final String DIR_PREFIX = "./src/main/resources";
    private final static String planSchemaFilePath = DIR_PREFIX + "/json/schema/jsonSchema.json";
    private final static String testJsonFilePath1 = DIR_PREFIX + "/json/testPlanJson1.json";
    private final static String testJsonFilePath2 = DIR_PREFIX + "/json/testPlanJson2.json";

    @Test
    public void testPJsonValidation(){
        String strJson1 = JsonFileUtil.read(testJsonFilePath1);
        // test json 2 missed the creationDate
        String strJson2 = JsonFileUtil.read(testJsonFilePath2);
        assertTrue(JsonValidateUtil.validateJson(planSchemaFilePath, strJson1));
        assertTrue(!JsonValidateUtil.validateJson(planSchemaFilePath, strJson2));
    }
}
