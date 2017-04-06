package edu.calpoly.apacheprojectdata.metrics;

import edu.calpoly.apacheprojectdata.ApacheProjectDataTest;
import edu.calpoly.apacheprojectdata.data.bugdatabase.BugDatabase;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.time.temporal.ChronoUnit;
import java.util.Arrays;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Mockito.*;

/**
 * Tests the bug databass class. All test data in {@link ProjectMetricsTest}.
 */
public class BugDatabaseMetricsTest extends ApacheProjectDataTest {

    private BugDatabase bd;

    @Before
    public void setup() {
        bd = mock(BugDatabase.class);

        when(bd.getAllIssues()).thenReturn(ProjectMetricsTest.ISSUES);
    }

    @Test
    public void testSetupEffortMetrics_fieldsCorrect() {
        BugDatabaseMetrics metrics = new BugDatabaseMetrics();
        metrics.setupEffortAndResolutionMetrics(bd.getAllIssues().values());

        assertEquals(ProjectMetricsTest.ISSUES.get(ProjectMetricsTest.EARLIEST_ISSUE_KEY).getDateCreated(), metrics.getEarliestTicket());
        assertEquals(ProjectMetricsTest.ISSUES.get(ProjectMetricsTest.LAST_ISSUE_KEY).getDateCreated(), metrics.getLastTicket());
        assertEquals(2 / (double) 5, metrics.getPercentTicketsEffortEstimate());
        assertEquals(3 / (double) 5, metrics.getPercentTicketsEffortSpend());
        assertEquals(1 / (double) 5, metrics.getPercentTicketsEffortEstimateAndSpent());
        assertEquals(5, (long) metrics.getTicketCount());
        assertEquals(3 / (double) 5, metrics.getPercentTicketResolved());
        assertEquals(3 / (double) ChronoUnit.DAYS.between(
                ProjectMetricsTest.ISSUES.get(ProjectMetricsTest.EARLIEST_ISSUE_KEY).getDateCreated(),
                ProjectMetricsTest.ISSUES.get(ProjectMetricsTest.LAST_ISSUE_KEY).getDateCreated()),
                metrics.getFrequencyCompletedTickets());
        assertEquals(ProjectMetricsTest.MONTHS_TRACKED, (long) metrics.getMonthsTracked());
        assertEquals(2, (long) metrics.getTicketsTypeBugs());
        assertEquals(1, (long) metrics.getTicketsTypeFeatures());
        assertEquals(1, (long) metrics.getTicketsTypeTest());
    }

    @Test
    public void testSetupPeopleInDatabase() {
        BugDatabaseMetrics metrics = new BugDatabaseMetrics();
        metrics.setupPeopleInDatabase(bd.getAllIssues().values());

        assertEquals(4, (long) metrics.getNumberPeopleInDatabase());
        assertEquals(1d, metrics.getAverageDevelopersPerCompleteTicket());
    }

    @Test
    public void testSetupByMonth_fieldsNotNull() {
        BugDatabaseMetrics metrics = new BugDatabaseMetrics();
        metrics.setupByMonthStats(bd.getAllIssues().values());
        assertNotNull(metrics.getMonthlyTickets());
        assertNotNull(metrics.getTicketResolutionTime());
    }

    @Test
    public void testSetupNumberFields_ValueCorrect() {
        ArgumentCaptor<double[]> argumentCaptor = ArgumentCaptor.forClass(double[].class);
        BugDatabaseMetrics metrics = new BugDatabaseMetrics();
        metrics = spy(metrics);
        metrics.setupNumberFields(bd.getAllIssues().values());
        assertEquals(10, (long) metrics.getNumberFields());
        verify(metrics, times(1)).createNumberMetrics(argumentCaptor.capture());
        double[] actual = argumentCaptor.getValue();
        double[] expected = {.1, .2, .1, .15, .2};
        Arrays.sort(actual);
        Arrays.sort(expected);
        assertArrayEquals(expected, actual, 0);
    }

    /*@Test
    public void testSetupAll_allSetupMethodsCalledIdNull() {
        BugDatabaseMetrics metrics = new BugDatabaseMetrics();
        metrics = spy(metrics);
        metrics.setupAll(Collections.singleton(bd));
        verify(metrics, times(1)).setupByMonthStats(bd.getAllIssues().values());
        verify(metrics, times(1)).setupPeopleInDatabase(bd.getAllIssues().values());
        verify(metrics, times(1)).setupEffortAndResolutionMetrics(bd.getAllIssues().values());
        verify(metrics, times(1)).setupNumberFields(bd.getAllIssues().values());
        //assertNull(metrics.getId());
    }*/
}