package edu.calpoly.apacheprojectdata.web;

import edu.calpoly.apacheprojectdata.metrics.MetricDescription;
import edu.calpoly.apacheprojectdata.metrics.MetricField;
import edu.calpoly.apacheprojectdata.metrics.MetricsManager;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Exposes snapshots to the API.
 */
@RestController
@RequestMapping("/fields")
public class FieldsController {

    @RequestMapping(method = RequestMethod.GET)
    public Map<MetricDescription.Group, List<MetricField>> getSnapshots() {
        return MetricsManager.getInstance().getFields();
    }
}
