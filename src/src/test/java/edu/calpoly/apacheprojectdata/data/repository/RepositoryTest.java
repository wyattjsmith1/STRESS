package edu.calpoly.apacheprojectdata.data.repository;

import edu.calpoly.apacheprojectdata.ApacheProjectDataTest;
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
 * Tests the {@link Repository} class.
 */
public class RepositoryTest extends ApacheProjectDataTest {

    private static final String GIT_REPOSITORY = "http://git-wip-us.apache.org/repos/asf?p=cassandra.git";
    private static final String REAL_GIT_REPOSITORY = "https://git1-us-west.apache.org/repos/asf/cassandra.git";

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testRepository_withGitRepo_allFieldsValid() throws IOException, URISyntaxException {
        File projectDir = temporaryFolder.newFolder();
        Project project = mock(Project.class);
        when(project.getProjectDirectory()).thenReturn(projectDir);
        Repository repo = Repository.create(GIT_REPOSITORY, project);

        assertTrue(repo instanceof GitRepository);
        assertEquals(new URI(REAL_GIT_REPOSITORY), repo.getRemoteRepository());
        assertEquals(new File(projectDir, "git"), repo.getLocalRepository());
    }
}