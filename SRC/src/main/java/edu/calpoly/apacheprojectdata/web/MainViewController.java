package edu.calpoly.apacheprojectdata.web;

import edu.calpoly.apacheprojectdata.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

/**
 * Renders the main page.
 */
@Controller
public class MainViewController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainViewController.class);
    private static String captchaPub;

    @RequestMapping("/")
    public String index(Model model) {
        model.addAttribute("captchaPub", captchaPub);
        return "index";
    }

    static {
        Properties props = new Properties();
        try {
            props.load(new FileReader(Settings.getApacheConfig()));
            captchaPub = props.getProperty("recaptcha.validation.publicKey");
        } catch (IOException e) {
            LOGGER.error("Unable to read properties", e);
        }
    }
}
