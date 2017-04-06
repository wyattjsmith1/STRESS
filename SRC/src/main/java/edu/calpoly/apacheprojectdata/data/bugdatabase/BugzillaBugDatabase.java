package edu.calpoly.apacheprojectdata.data.bugdatabase;

import edu.calpoly.apacheprojectdata.data.Issue;
import edu.calpoly.apacheprojectdata.data.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;

/**
 * Bugzilla database. Not yet implemented.
 */
public class BugzillaBugDatabase extends BugDatabase {

    BugzillaBugDatabase(URI url, Project project) throws URISyntaxException {
        super(url, project);
    }

    @NotNull
    @Override
    public String getTicketIdRexEx() {
        return "";
    }

    @Nullable
    @Override
    public String getRoot() {
        return null;
    }

    @Override
    public Map<String, Issue> getAllIssues() {
        return Collections.emptyMap();
    }

    @Override
    public boolean updateIssues() {
        return true;
    }

    @Override
    public BugDatabaseType getType() {
        return BugDatabaseType.BUGZILLA;
    }

    @Override
    public Long getNumberReleases() {
        return 0L;
    }
}
