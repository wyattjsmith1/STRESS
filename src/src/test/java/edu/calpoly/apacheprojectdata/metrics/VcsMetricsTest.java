package edu.calpoly.apacheprojectdata.metrics;

import edu.calpoly.apacheprojectdata.data.repository.Repository;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.util.Collections;

import static junit.framework.TestCase.*;
import static org.mockito.Mockito.*;

/**
 * Testing for vcs metrics. All test data in {@link ProjectMetricsTest}.
 */
public class VcsMetricsTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private Repository repo;

    @Before
    public void setup() throws IOException, GitAPIException {
        repo = mock(Repository.class);
        when(repo.getRevisions()).thenReturn(ProjectMetricsTest.REVISIONS);
        when(repo.getLocalRepository()).thenReturn(temporaryFolder.newFolder());
    }

    @Test
    public void testSetupFirstLast_firstLastCorrect() throws IOException, GitAPIException {
        VcsMetrics metrics = new VcsMetrics();
        metrics.setupFirstLast(repo.getRevisions());
        assertEquals(ProjectMetricsTest.REVISIONS.get(ProjectMetricsTest.FIRST_COMMIT_INDEX).getDate(), metrics.getEarliestCommit());
        assertEquals(ProjectMetricsTest.REVISIONS.get(ProjectMetricsTest.LAST_COMMIT_INDEX).getDate(), metrics.getLastCommit());
    }

    @Test
    public void testSetupAuthors_correctNumberAuthors() throws IOException, GitAPIException {
        VcsMetrics metrics = new VcsMetrics();
        metrics.setupAuthors(repo.getRevisions());
        assertEquals(4, (long) metrics.getNumberAuthors());
    }

    @Test
    public void testSetupCommitCount_correctCountMonthlyNotNull() throws IOException, GitAPIException {
        VcsMetrics metrics = new VcsMetrics();
        metrics.setupCommitCounts(repo.getRevisions());
        assertEquals(5, (long) metrics.getCommitCount());
        assertNotNull(metrics.getMonthlyCommits());
    }

    @Test
    public void testSetupAll_allSetupMethodsCalled() throws IOException, GitAPIException {
        VcsMetrics metrics = new VcsMetrics();
        metrics = spy(metrics);
        metrics.setupAll(Collections.singleton(repo));

        verify(metrics, times(1)).setupFirstLast(repo.getRevisions());
        verify(metrics, times(1)).setupCommitCounts(repo.getRevisions());
        verify(metrics, times(1)).setupAuthors(repo.getRevisions());
        verify(metrics, times(1)).setupFileCount(Collections.singleton(repo));
        //assertNull(metrics.getId());
    }
}