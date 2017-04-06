package edu.calpoly.apacheprojectdata.metrics;

import edu.calpoly.apacheprojectdata.ApacheProjectDataTest;
import edu.calpoly.apacheprojectdata.data.Issue;
import edu.calpoly.apacheprojectdata.data.Project;
import edu.calpoly.apacheprojectdata.data.Revision;
import edu.calpoly.apacheprojectdata.data.bugdatabase.BugDatabase;
import edu.calpoly.apacheprojectdata.data.bugdatabase.JiraBugDatabase;
import edu.calpoly.apacheprojectdata.data.repository.GitRepository;
import edu.calpoly.apacheprojectdata.data.repository.Repository;
import edu.calpoly.apacheprojectdata.util.FileUtil;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

import static junit.framework.TestCase.*;
import static org.mockito.Mockito.*;

/**
 * Tests project metrics. Note, parse pom will fail because I am not making a fake pom.xml.
 */
public class ProjectMetricsTest extends ApacheProjectDataTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectMetricsTest.class);

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private static final String FAKE_PROJECT_NAME = "Fake project name";
    private static final String FAKE_HOMEPAGE = "www.homepage.com";

    static final String EARLIEST_ISSUE_KEY = "5";
    static final String LAST_ISSUE_KEY = "1";
    static final int FIRST_COMMIT_INDEX = 2;
    static final int LAST_COMMIT_INDEX = 4;
    static final int MONTHS_TRACKED = 3;

    static final Map<String, Issue> ISSUES;
    static {
        ISSUES = new HashMap<>();
        ISSUES.put("1", new Issue("1", "user1", Collections.singleton("user1"), null, 5, "Open", Issue.Type.BUG, 10, .1, new HashMap<>(), new HashMap<>(), ZonedDateTime.now(ZoneOffset.UTC), null));
        ISSUES.put("2", new Issue("2", "user2", new HashSet<>(), 0, 0, "Open", Issue.Type.BUG,10, .2, new HashMap<>(), new HashMap<>(), ZonedDateTime.now(ZoneOffset.UTC).minusDays(1), null));
        ISSUES.put("3", new Issue("3", "user1", Collections.singleton("user2"), 5, 0, "Resolved", Issue.Type.FEATURE,10,.1, new HashMap<>(), new HashMap<>(), ZonedDateTime.now(ZoneOffset.UTC).minusDays(2),  ZonedDateTime.now(ZoneOffset.UTC).minusDays(1)));
        ISSUES.put("4", new Issue("4", "user3", Collections.singleton("user2"), 5, 5, "Resolved", Issue.Type.TEST,10, .15, new HashMap<>(), new HashMap<>(), ZonedDateTime.now(ZoneOffset.UTC).minusDays(8),  ZonedDateTime.now(ZoneOffset.UTC).minusDays(2)));
        ISSUES.put("5", new Issue("5", "user3", Collections.singleton("user4"), 5, 0, "Resolved", Issue.Type.OTHER,9, .2, new HashMap<>(), new HashMap<>(), ZonedDateTime.now(ZoneOffset.UTC).minusMonths(MONTHS_TRACKED), ZonedDateTime.now(ZoneOffset.UTC).minusMonths(MONTHS_TRACKED).minusDays(2)));
    }

    static final List<Revision> REVISIONS = Arrays.asList(
            new Revision(1, Collections.singleton("3"), "user2", 1, ZonedDateTime.now().minusDays(1)),
            new Revision(2, Collections.singleton("4"), "user5", 2, ZonedDateTime.now().minusDays(2)),
            new Revision(3, Collections.singleton("5"), "user4", 3, ZonedDateTime.now().minusDays(3)),
            new Revision(4, Collections.singleton("5"), "user4", 4, ZonedDateTime.now().minusDays(2)),
            new Revision(5, new HashSet<>(), "user6", 5, ZonedDateTime.now())
    );

    private Project project;
    private GitRepository git;
    private JiraBugDatabase jira;

    @Before
    public void setup() throws  IOException, GitAPIException {
        // Otherwise the program will try to read all the languages, but there is no real code.
        LanguageMetrics.FILE_TYPE.clear();
        project = mock(Project.class);
        git = mock(GitRepository.class);
        jira = mock(JiraBugDatabase.class);
        File projectDir = temporaryFolder.newFolder();
        File gitDir = new File(projectDir, GitRepository.GIT_FILENAME);
        if (!gitDir.mkdir()) {
            LOGGER.error("Unable to create fake gitDir");
            fail();
        }

        when(project.getRepositories()).thenReturn(Collections.singleton(git));
        when(project.getBugDatabases()).thenReturn(Collections.singleton(jira));
        when(project.getName()).thenReturn(FAKE_PROJECT_NAME);
        when(project.getHomepage()).thenReturn(FAKE_HOMEPAGE);
        when(project.getProjectDirectory()).thenReturn(projectDir);

        when(jira.getAllIssues()).thenReturn(ISSUES);
        when(jira.getType()).thenReturn(BugDatabase.BugDatabaseType.JIRA);
        when(git.getRevisions()).thenReturn(REVISIONS);
        when(git.getType()).thenReturn(Repository.RepoType.GIT);
        when(git.getLocalRepository()).thenReturn(gitDir);
    }

    @Test
    public void testSetProject_homepageNameBuildJira() throws InterruptedException, GitAPIException, IOException {
        ProjectMetrics metrics = new ProjectMetrics();
        metrics.setupProjectFields(project);
        assertEquals(FAKE_HOMEPAGE, metrics.getHomepage());
        assertEquals(1, (long) metrics.getNumberGitRepos());
        assertEquals(0, (long) metrics.getNumberSvnRepos());
        assertEquals(1, (long) metrics.getNumberJiraDatabases());
    }

    @Test
    public void testSetupLinkage_linkageFieldsCorrect() throws IOException, GitAPIException {
        ProjectMetrics metrics = new ProjectMetrics();
        metrics.setupLinkage(git.getRevisions(), jira.getAllIssues().values());
        assertEquals(4, (long) metrics.getVcsMatchesTicket());
        assertEquals(3 / (double) 5, metrics.getPercentTicketsReferencedInVcs());
    }

    @Test
    public void testSetupLanguages_lengthLonugagesIsExtensions() {
        // All values will be 0, but that is fine for testing. the number of programming languages will be 0, but
        // testing will require a lot of code.
        ProjectMetrics metrics = new ProjectMetrics();
        metrics.setupLanguages(project);
        assertEquals(LanguageMetrics.FILE_TYPE.size(), metrics.getLanguages().size());
    }

    @Test
    public void testBuilds_correctOnPomOrBuildXml() throws IOException {
        ProjectMetrics metrics = new ProjectMetrics();
        createBuildFile(FileUtil.POM);
        metrics.setupBuilds(project);
        assertTrue(metrics.getUsesMaven());
        assertFalse(metrics.getUsesAnt());
        assertNotNull(metrics.getLibraries());


        createBuildFile(FileUtil.ANT);
        metrics.setupBuilds(project);
        assertFalse(metrics.getUsesMaven());
        assertTrue(metrics.getUsesAnt());
    }

    @Test
    public void testSetupAll_callsSetupMethodsMetricsNotNull() throws IOException, GitAPIException {
        ProjectMetrics metrics = new ProjectMetrics();
        metrics = spy(metrics);
        metrics.setupAll(project);
        verify(metrics, times(1)).setupBuilds(project);
        verify(metrics, times(1)).setupLinkage(git.getRevisions(), new LinkedList<>(jira.getAllIssues().values()));
        verify(metrics, times(1)).setupLanguages(project);
        verify(metrics, times(1)).setupProjectFields(project);
        assertNotNull(metrics.getVcs());
        assertNotNull(metrics.getBugDatabase());
        assertNull(metrics.getId());
    }

    private void createBuildFile(String filename) throws IOException {
        FileUtils.cleanDirectory(git.getLocalRepository());
        File build = new File(git.getLocalRepository(), filename);
        if (!build.createNewFile()) {
            LOGGER.error("Unable to create fake build file");
            fail();
        }
    }
}