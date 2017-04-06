package edu.calpoly.apacheprojectdata.data.bugdatabase;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.calpoly.apacheprojectdata.Settings;
import edu.calpoly.apacheprojectdata.data.Issue;
import edu.calpoly.apacheprojectdata.data.Project;
import edu.calpoly.apacheprojectdata.util.StringUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.StreamSupport;

/**
 * Represents a Jira database.
 */
public class JiraBugDatabase extends BugDatabase {

    private static final String JIRA_ROOT = "https://issues.apache.org/jira/";
    private static final String JIRA_JQL = "rest/api/2/search";
    private static final String JIRA_RELEASES = "rest/api/2/project/%s/versions";
    private static final String ISSUES_FIELD_JIRA = "issues";
    private static final int MAX_RESULTS = 1000; // Max determined by JIRA. I wish it was greater.
    private static final Logger LOGGER = LoggerFactory.getLogger(JiraBugDatabase.class);
    private static final int MAX_RETRIES = 10;
    private static final int HTTP_TIMEOUT = 60000;
    private static final RequestConfig requestConfig;
    private static String auth;
    private Long numberReleases;
    private Map<String, Issue> issues;

    JiraBugDatabase(URI url, Project project) throws URISyntaxException {
        super(url, project);
    }

    @Override
    public String getTicketIdRexEx() {
        return String.format("%s-\\d+", getProjectName());
    }

    @Override
    public boolean updateIssues() {
        BugSave save = loadIssues();
        long updated = -1;
        issues = new HashMap<>();
        if (save != null) {
            updated = save.getUpdated();
            issues = save.getIssues();
        }

        // Save the start time for future searches. Using start time because tickets can be updated during search.
        long started = System.currentTimeMillis();

        ObjectNode first = getIssueSet(0, updated);
        if (first == null) {
            LOGGER.error("Unable to update bug list for " + getProject().getName());
            return false;
        }
        Long total = first.get("total").asLong();

        if (total > MAX_RESULTS) {
            List<Long> indices = new LinkedList<>();
            for (long i = MAX_RESULTS; i <= total; i += MAX_RESULTS) {
                indices.add(i);
            }
            long finalUpdated = updated;
            indices.stream()
                    .parallel()
                    .forEach(index -> getIssueSet(index, finalUpdated));
        }
        setupNumberReleases();
        saveIssues(started);
        return true;
    }

    private void setupNumberReleases() {
        numberReleases = 0L;
        InputStream stream = null;
        JsonNode result;
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpGet get = new HttpGet(getRoot() + String.format(JIRA_RELEASES, getProjectName()));
            get.addHeader("Content-Type", "application/json");
            get.addHeader("Authorization", auth);
            HttpResponse response = httpclient.execute(get);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                try {
                    stream = entity.getContent();
                    result = StringUtil.MAPPER.readValue(stream, ArrayNode.class);
                    numberReleases = (long) result.size();
                } finally {
                    if (stream != null) {
                        stream.close();
                    }
                }
            }
        } catch (IllegalArgumentException | IOException e) {
            LOGGER.error("Unable to download database", e);
        }
    }

    @Override
    public BugDatabaseType getType() {
        return BugDatabaseType.JIRA;
    }

    @Override
    public Long getNumberReleases() {
        // In one case, this is null. I have no idea why.
        if (numberReleases == null) {
            numberReleases = 0L;
        }
        return numberReleases;
    }

    @Nullable
    private String getProjectName() {
        switch (getProject().getName()) {
            case "Apache XMLBeans":
                return "XMLBEANS";
            case "Apache Excalibur":
                return "EXLBR";
            case "Apache Hivemind":
                return "HIVEMIND";
            case "Apache Jakarta Cactus":
                return "CACTUS";
            default:
                String[] segments = getLocation().getPath().split("/");
                return segments[segments.length - 1];
        }
    }

    @Override
    @Nullable
    public String getRoot() {
        return JIRA_ROOT;
    }

    @Override
    public Map<String, Issue> getAllIssues() {
        return issues;
    }

    /**
     * Gets a single set of issues from the database and adds them to the map. Returns the json from the server.
     * @param index The index to query for starting at 0.
     * @param updated Anything updated after this point.
     * @return The json the server returns.
     */
    @Nullable
    private ObjectNode getIssueSet(long index, long updated) {
        String dateQuery = updated < 0 ? "" : "AND updated > " + updated;
        String query = String.format("project = \"%s\" %s", getProjectName(), dateQuery);
        ObjectNode current = executeQuery(query, index, MAX_RESULTS);
        if (current == null) {
            LOGGER.warn("Failed to download projects.");
            return null;
        }

        if (current.get("errorMessages") != null) {
            StringBuilder messages = new StringBuilder();
            for (JsonNode n : current.get("errorMessages")) {
                messages.append(n.asText());
                messages.append(", ");
            }
            // Built in formatting not working for some reason.
            LOGGER.warn("Unable to get issue set: " + messages);
            return null;
        }

        current.get(ISSUES_FIELD_JIRA).forEach(obj -> {
            Issue newIssue = createIssue(obj);
            issues.put(newIssue.getId(), newIssue);
        });
        return current;
    }

    private static Issue.Type getType(JsonNode type) {
        switch (type.get("name").asText()) {
            case "Bug": return Issue.Type.BUG;
            case "Test": return Issue.Type.TEST;
            case "New-Feature":
            case "Improvement": return Issue.Type.FEATURE;
            default: return Issue.Type.OTHER;
        }
    }

    @NotNull
    private static Issue createIssue(JsonNode obj) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        JsonNode fields = obj.get("fields");
        Issue issue = new Issue();
        issue.setId(obj.get("key").asText());
        issue.setEffortEstimated(fields.get("timeestimate").asInt());
        issue.setEffortSpent(fields.get("timespent").asInt());
        issue.setStatus(fields.get("status").get("name").asText());
        issue.setFieldsReturned(fields.size());
        issue.setType(getType(fields.get("issuetype")));
        issue.setAssigned(new HashSet<>());
        issue.setPercentFieldsUsed(StreamSupport.stream(fields.spliterator(), false)
                .filter(j -> !j.isNull())
                .count()
                / (double) fields.size());
        if (!fields.get("assignee").isNull()) {
            issue.getAssigned().add(fields.get("assignee").get("name").asText());
        }

        if (!fields.get("reporter").isNull()) {
            issue.setReporter(fields.get("reporter").get("name").asText());
        } else {
            issue.setReporter(null);
        }

        String date = fields.get("created").asText();
        issue.setDateCreated(ZonedDateTime.parse(date, formatter));
        date = fields.get("resolutiondate").asText();
        // Jackson returns null strings as "null" instead of java null.
        if (date != null && !"null".equalsIgnoreCase(date)) {
            issue.setResolutionDate(ZonedDateTime.parse(date, formatter));
        }

        issue.setFieldsUsed(new HashMap<>());
        issue.setNumericFields(new HashMap<>());
        StreamSupport.stream(Spliterators.spliteratorUnknownSize(fields.fieldNames(), Spliterator.ORDERED), false)
                .forEach(x -> {
                    JsonNode field = fields.get(x);
                    issue.getFieldsUsed().put(x, field != null && !field.isNull());
                    if (field.isNumber()) {
                        issue.getNumericFields().put(x, field.doubleValue());
                    }
                });

        return issue;
    }

    /**
     * Executes a JQL query and returns tho json representing the data. Will retry {@link JiraBugDatabase#MAX_RETRIES} times.
     * @param jql The JQL to execute.
     * @param index The starting index of the data to page to (0 indexed)
     * @param maxResults The maximum number of results to return. No more than {@link JiraBugDatabase#MAX_RESULTS}.
     * @return The json representing the data.
     */
    @Nullable
    private ObjectNode executeQuery(String jql, long index, int maxResults) {
        ObjectNode postArgs = StringUtil.MAPPER.createObjectNode();
        postArgs.put("jql", jql);
        postArgs.put("startAt", index);
        postArgs.put("maxResults", maxResults);

        InputStream stream = null;
        ObjectNode result = null;
        int retries = 0;
        while (result == null && retries++ < MAX_RETRIES) {
            try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
                HttpPost post = new HttpPost(getRoot() + JIRA_JQL);
                post.addHeader("Content-Type", "application/json");
                post.addHeader("Authorization", auth);
                post.setConfig(requestConfig);
                post.setEntity(new ByteArrayEntity(postArgs.toString().getBytes()));
                HttpResponse response = httpclient.execute(post);
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    try {
                        stream = entity.getContent();
                        result = StringUtil.MAPPER.readValue(stream, ObjectNode.class);
                    } finally {
                        if (stream != null) {
                            stream.close();
                        }
                    }
                }
            } catch (IllegalArgumentException | IOException e) {
                LOGGER.error("Unable to download database", e);
            }
        }
        return result;
    }

    /*
        Reads in the apache JIRA configuration file.
    */
    static {
        initAuth();

        requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(HTTP_TIMEOUT)
                .setConnectTimeout(HTTP_TIMEOUT)
                .setSocketTimeout(HTTP_TIMEOUT)
                .build();
    }

    /**
     * Initializes the authentication. This is out of the static block for testing reasons.
     */
    private static void initAuth() {
        InputStream stream;
        try {
            Properties prop = new Properties();
            stream = new FileInputStream(Settings.getApacheConfig());
            prop.load(stream);
            String uname = prop.getProperty("apache.username");
            String pword = prop.getProperty("apache.password");

            if (uname == null || pword == null) {
                throw new InvalidPropertiesFormatException("Invalid properties file");
            }
            String encode = uname + ":" + pword;
            auth = "Basic " + Base64.getEncoder().encodeToString(encode.getBytes());
            stream.close();
        } catch (IOException e) {
            LOGGER.error("Unable to read your Apache properties file. Did you include one?", e);
            auth = null;
        }
    }
}
