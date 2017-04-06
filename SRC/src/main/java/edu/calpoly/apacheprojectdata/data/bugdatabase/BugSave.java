package edu.calpoly.apacheprojectdata.data.bugdatabase;

import edu.calpoly.apacheprojectdata.data.Issue;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * The data that goes in bugs.json. Class used to make Jackson serialization easier.
 */
@Data
@NoArgsConstructor
class BugSave {

    private long updated;
    private Map<String, Issue> issues;
}
