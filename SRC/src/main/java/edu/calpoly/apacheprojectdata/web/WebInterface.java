package edu.calpoly.apacheprojectdata.web;

import edu.calpoly.apacheprojectdata.Settings;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;


/**
 * Creates a web interface for searching.
 */
@SpringBootApplication
public class WebInterface {

    public void run() throws IOException {
        Properties properties = new Properties();
        properties.load(new FileReader(Settings.getApacheConfig()));
        new SpringApplicationBuilder()
                .properties(properties)
                .sources(WebInterface.class)
                .run();
    }
}