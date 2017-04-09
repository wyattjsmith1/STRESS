package edu.calpoly.apacheprojectdata.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.calpoly.apacheprojectdata.metrics.Filter;
import lombok.Data;

import java.util.List;

@Data
public class FilterSearch {

    private List<Filter> filters;

    @JsonProperty("g-recaptcha-response")
    private String captcha;
}
