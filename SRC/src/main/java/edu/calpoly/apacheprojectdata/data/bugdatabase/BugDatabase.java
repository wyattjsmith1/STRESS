package edu.calpoly.apacheprojectdata.data.bugdatabase;

import edu.calpoly.apacheprojectdata.data.Issue;
import edu.calpoly.apacheprojectdata.data.Project;
import edu.calpoly.apacheprojectdata.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Map;

/**
 * Represents a bug database.
 */
public abstract class BugDatabase {

    private static final String BUG_FILE_NAME = "bugs.json";
    private static final String JIRA_ROOT = "https://issues.apache.org/jira/";
    private static final String BUG_FILE = "bugs.json";
    private static final Logger LOGGER = LoggerFactory.getLogger(BugDatabase.class);

    private URI location;
    private Project project;

    public enum BugDatabaseType {
        JIRA, BUGZILLA
    }

    BugDatabase(URI url, Project project) throws URISyntaxException {
        this.location = url;
        this.project = project;
    }

    @NotNull
    public static BugDatabase create(String url, Project project) throws URISyntaxException {
        URI location = new URI(url.replace("http://", "https://"));
        if (location.toString().startsWith(JIRA_ROOT)) {
            return new JiraBugDatabase(location, project);
        } else {
            return new BugzillaBugDatabase(location, project);
        }
    }

    /**
     * Gets a reference to the local bug file.
     * @return A reference to the local bug file.
     */
    @NotNull
    File getLocalBugFile() {
        return new File(project.getProjectDirectory(), BUG_FILE_NAME);
    }

    /**
     * Gets a regular expression representing a ticket id.
     * @return The regular expression.
     */
    @Nullable
    public abstract String getTicketIdRexEx();

    /**
     * Gets the bug database root URL.
     * @return The URL.
     */
    @Nullable
    public abstract String getRoot();

    /**
     * Get the url passed in as a {@link URI}.
     * @return A {@link URI} representing the remote bug database.
     */
    @NotNull
    URI getLocation() {
        return location;
    }

    /**
     * Gets the project associated with the bug database.
     * @return The {@link Project} associated with the database.
     */
    public Project getProject() {
        return project;
    }

    /**
     * Returns all the issues in the bug database.
     * @return A {@link Collection} of {@link Issue}s in the database.
     */
    public abstract Map<String, Issue> getAllIssues();

    /**
     * Tells the bug database to update all the bugs. The data should be saved locally in
     * {@link BugDatabase#getSaveFile()}.
     */
    public abstract boolean updateIssues();

    /**
     * Returns the location to save the data.
     * @return The location of the bug file.
     */
    @NotNull
    private File getSaveFile() {
        return new File(project.getProjectDirectory(), BUG_FILE);
    }

    protected void saveIssues(long updated) {
        try  {
            BugSave save = new BugSave();
            save.setIssues(getAllIssues());
            save.setUpdated(updated);
            StringUtil.MAPPER.writeValue(getSaveFile(), save);
        } catch (IOException e) {
            LOGGER.error("Unable to save bug file", e);
        }
    }

    /**
     * Gets the issues that are saved to disk in JSON.
     * @return A {@link BugSave} representing the issues or null if they could not be found.
     */
    @Nullable
    protected BugSave loadIssues() {
        BugSave result;
        File file = getSaveFile();
        if (!file.exists()) {
            return null;
        }
        try {
            result = StringUtil.MAPPER.readValue(file, BugSave.class);
        } catch (Exception e) {
            LOGGER.info("No Bug file found", e);
            result = null;
        }
        return result;
    }

    public abstract BugDatabaseType getType();

    public abstract Long getNumberReleases();
}
