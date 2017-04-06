package edu.calpoly.apacheprojectdata.data.bugdatabase;

import edu.calpoly.apacheprojectdata.ApacheProjectDataTest;
import edu.calpoly.apacheprojectdata.Settings;
import edu.calpoly.apacheprojectdata.data.Project;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link BugDatabase}.
 */
public class BugDatabaseTest extends ApacheProjectDataTest {

    private static final String FAKE_JIRA = "https://issues.apache.org/jira/browse/CASSANDRA";

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testConstructor_Jira_allFieldsCorrect() throws URISyntaxException, IOException {
        Settings.setDataDir(temporaryFolder.newFolder());
        File projectDir = temporaryFolder.newFolder();
        Project project = mock(Project.class);
        when(project.getProjectDirectory()).thenReturn(projectDir);

        BugDatabase jira = BugDatabase.create(FAKE_JIRA, project);

        assertTrue(jira instanceof JiraBugDatabase);
        assertEquals(new File(projectDir, "bugs.json"), jira.getLocalBugFile());
        assertEquals("https://issues.apache.org/jira/", jira.getRoot());
        assertEquals(new URI(FAKE_JIRA), jira.getLocation());
    }
}