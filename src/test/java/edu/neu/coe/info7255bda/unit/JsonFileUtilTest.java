package edu.neu.coe.info7255bda.unit;

import static org.junit.Assert.*;

import edu.neu.coe.info7255bda.utils.JsonFileUtil;
import org.junit.jupiter.api.Test;

public class JsonFileUtilTest {
    public static final String DIR_PREFIX = "./src/main/resources";
    private final static String testJsonFilePath = DIR_PREFIX + "/json/testReadJson.json";
    private final static String JsonString = "\n" + "{\n" +
            "  \"1\": 1,\n" +
            "  \"2\": \"2\",\n" +
            "  \"test\": [\n" +
            "    1, 2, \"test\"\n" +
            "  ]\n" +
            "}";

    @Test
    public void testJsonFileRead(){
        String strJson = JsonFileUtil.read(testJsonFilePath);
        assertEquals(strJson, JsonString);
    }
}
