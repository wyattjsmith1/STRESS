package edu.calpoly.apacheprojectdata.data;

import com.fasterxml.jackson.databind.JsonNode;
import edu.calpoly.apacheprojectdata.Settings;
import edu.calpoly.apacheprojectdata.data.bugdatabase.BugDatabase;
import edu.calpoly.apacheprojectdata.data.repository.Repository;
import edu.calpoly.apacheprojectdata.util.StringUtil;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a project with the required information.
 */
public class Project {

    private static final String REPO_URL = "https://projects.apache.org/json/foundation/projects.json";

    private static final Logger LOGGER = LoggerFactory.getLogger(Project.class);
    private static final String BUG_DATABASE = "bug-database";
    private static final String REPOSITORY = "repository";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_HOMEPAGE = "homepage";

    @Getter
    private Collection<Repository> repositories;

    @Getter
    private Collection<BugDatabase> bugDatabases;

    @Getter
    private String name;

    @Getter
    private String homepage;

    private Project() {
        /* Not public. */
    }

    /**
     * Creates a {@link Project} object from Apache JSON object.
     * @param obj The JSON object from Apache to convert into an object.
     * @return A Project representing the data. If a field is missing, it will be null. Fields must contain the correct
     *         type of data though ({@link String}s must be {@link String}s, ...).
     */
    @Nullable
    private static Project fromJson(@NotNull JsonNode obj) {
        Project project = new Project();

        project.name = obj.get(FIELD_NAME).asText();
        if (StringUtil.stringIsEmpty(project.name)) {
            return null;
        }

        if (!obj.hasNonNull(BUG_DATABASE)) {
            return null;
        }
        String bug = obj.get(BUG_DATABASE).asText();
        project.bugDatabases = new HashSet<>();
        if (!StringUtil.stringIsEmpty(bug)) {
            try {
                // Some projects have multiple repos separated by comma
                for (String repo : bug.split(",")) {
                    BugDatabase next = BugDatabase.create(URLDecoder.decode(repo.trim(), "UTF-8"), project);
                    if (next != null) {
                        project.bugDatabases.add(next);
                    }
                }
            } catch (UnsupportedEncodingException | URISyntaxException e) {
                // Happens when there are multiple bug databases. (not yet supported)
                LOGGER.error("Unable to parse bug database: " + bug, e);
            }
        }

        JsonNode repoArray = obj.get(REPOSITORY);
        project.repositories = new HashSet<>();
        if (repoArray != null) {
            repoArray.forEach(r -> {
                try {
                    String repoUrl = URLDecoder.decode(r.asText(), "UTF-8");
                    Repository next = Repository.create(repoUrl, project);
                    if (next != null) {
                        project.repositories.add(next);
                    }
                } catch (UnsupportedEncodingException | URISyntaxException e) {
                    LOGGER.error("Unable to parse repo: " + r.asText(), e);
                }
            });
        }
        project.homepage = obj.get(FIELD_HOMEPAGE).asText();
        return project;
    }

    @Override
    public String toString() {
        return getName();
    }

    @NotNull
    public File getProjectDirectory() {
        File file = new File(Settings.getDataDir(), getName().replace(" ", ""));
        if (!file.exists() && !file.mkdirs()) {
            LOGGER.error("Unable to create project dir: " + file.getAbsolutePath());
        }
        return file;
    }

    public static Collection<Project> getProjects(@Nullable Collection<String> projects) throws IOException {
        JsonNode apache = StringUtil.MAPPER.readValue(new URL(REPO_URL), JsonNode.class);
        Collection<Project> result = new LinkedList<>();

        if (projects == null || projects.isEmpty()) {
            Collection<Project> finalResult = result;
            apache.fields().forEachRemaining(entry -> finalResult.add(fromJson(entry.getValue())));
            result = result.stream().filter(Objects::nonNull).collect(Collectors.toList());
        } else {
            result = projects.stream()
                    .map(apache::get)
                    .map(Project::fromJson)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        return result;
    }


    public void freeResources() {
        repositories = Collections.emptyList();
        bugDatabases = Collections.emptyList();
    }
}
