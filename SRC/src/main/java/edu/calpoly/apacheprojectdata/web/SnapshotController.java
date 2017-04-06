package edu.calpoly.apacheprojectdata.web;

import edu.calpoly.apacheprojectdata.metrics.MetricsManager;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Exposes snapshots to the API.
 */
@RestController
@RequestMapping("/snapshots")
public class SnapshotController {

    @RequestMapping(method = RequestMethod.GET)
    public List getSnapshots() {
        return MetricsManager.getInstance().getSnapshots();
    }
}
