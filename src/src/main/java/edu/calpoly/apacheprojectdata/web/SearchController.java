package edu.calpoly.apacheprojectdata.web;

import edu.calpoly.apacheprojectdata.metrics.Filter;
import edu.calpoly.apacheprojectdata.metrics.MetricsManager;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Handles search requests.
 */
@RestController
@RequestMapping("/search")
public class SearchController {

    @RequestMapping(value = "/{snapshot}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List search(@RequestBody List<Filter> filters, @PathVariable("snapshot") Integer snapshot) {
        return MetricsManager.getInstance().search(filters, snapshot);
    }
}
