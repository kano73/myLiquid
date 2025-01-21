package org.example;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

public class GetProperties {

    public static Properties get() {
        Properties properties = new Properties();
        String filename = "\"src/main/resources/application.properties\"";
        try (FileInputStream input = new FileInputStream(filename)) {
            properties.load(input);
        }catch (IOException e){
            throw new RuntimeException("an error accused while trying to read application.properties from: "+filename+"\n" +
                    e.getMessage()+"\n"+
                    Arrays.toString(e.getStackTrace()));
        }
        return properties;
    }

}
