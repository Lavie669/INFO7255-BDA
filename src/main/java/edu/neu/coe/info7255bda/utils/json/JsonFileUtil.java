package edu.neu.coe.info7255bda.utils.json;

import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;

@Component
public class JsonFileUtil {

    public static String read(String filePath){
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
}
