package edu.calpoly.apacheprojectdata.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Renders the main page.
 */
@Controller
public class MainViewController {

    @RequestMapping("/")
    public String index(Model model) {
        return "index";
    }
}
