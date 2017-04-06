package edu.calpoly.apacheprojectdata.metrics;

import edu.calpoly.apacheprojectdata.data.Issue;
import edu.calpoly.apacheprojectdata.data.bugdatabase.BugDatabase;
import lombok.Getter;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Used for querying data for a bug database.
 */
@Embeddable
@Getter
class BugDatabaseMetrics extends Metrics {


    @MetricDescription(displayName = "Percent Tickets Resolved", group = MetricDescription.Group.SIZE)
    @Column private Double percentTicketResolved;

    @MetricDescription(displayName = "Percent Tickets With Effort Spent", group = MetricDescription.Group.SIZE)
    @Column private Double percentTicketsEffortSpend;

    @MetricDescription(displayName = "Percent Tickets With Effort Estimated", group = MetricDescription.Group.SIZE)
    @Column private Double percentTicketsEffortEstimate;

    @MetricDescription(displayName = "Percent Tickets With Effort Spent And Estimated", group = MetricDescription.Group.SIZE)
    @Column private Double percentTicketsEffortEstimateAndSpent;

    @MetricDescription(displayName = "Earliest Ticket Date", group = MetricDescription.Group.ACTIVITY)
    @Column private ZonedDateTime earliestTicket;

    @MetricDescription(displayName = "Last Ticket Date", group = MetricDescription.Group.ACTIVITY)
    @Column private ZonedDateTime lastTicket;

    @MetricDescription(displayName = "Number Tickets", group = MetricDescription.Group.ACTIVITY)
    @Column private Integer ticketCount;

    @MetricDescription(displayName = "Tickets Completed Per Day", group = MetricDescription.Group.ACTIVITY)
    @Column private Double frequencyCompletedTickets;

    @MetricDescription(displayName = "People In Bug Database", group = MetricDescription.Group.ACTIVITY)
    @Column private Integer numberPeopleInDatabase;

    @MetricDescription(displayName = "Number Of Months Tracked", group = MetricDescription.Group.ACTIVITY)
    @Column private Long monthsTracked;

    @MetricDescription(displayName = "Number Of Fields For Each Ticket", group = MetricDescription.Group.SIZE)
    @Column private Long numberFields;

    @MetricDescription(displayName = "Number Tickets Resolved", group = MetricDescription.Group.SIZE)
    @Column private Long ticketsResolved;

    @MetricDescription(displayName = "Number Test Tickets", group = MetricDescription.Group.SIZE)
    @Column private Long ticketsTypeTest;

    @MetricDescription(displayName = "Number Bug Tickets", group = MetricDescription.Group.SIZE)
    @Column private Long ticketsTypeBugs;

    @MetricDescription(displayName = "Number Feature Tickets", group = MetricDescription.Group.SIZE)
    @Column private Long ticketsTypeFeatures;

    @MetricDescription(displayName = "Number Of Releases", group = MetricDescription.Group.ACTIVITY)
    @Column private Long numberReleases;

    @MetricDescription(displayName = "Average Developers Per Completed Ticket", group = MetricDescription.Group.ACTIVITY)
    @Column private Double averageDevelopersPerCompleteTicket;

    @ElementCollection
    private Map<String, NumberMetrics> numericFields;

    @ElementCollection
    private Map<String, Double> fieldAvailability;

    @Embedded
    @MetricDescription(displayName = "Monthly Tickets")
    @AttributeOverrides({
            @AttributeOverride(name = "mean", column = @Column(name = "monthlyTicketsMean")),
            @AttributeOverride(name = "median", column = @Column(name = "monthlyTicketsMedian")),
            @AttributeOverride(name = "iqr", column = @Column(name = "monthlyTicketsIqr")),
            @AttributeOverride(name = "stdDev", column = @Column(name = "monthlyTicketsStdDev"))
    })
    private NumberMetrics monthlyTickets;
    @Embedded
    @MetricDescription(displayName = "Ticket Resolution Time")
    @AttributeOverrides({
            @AttributeOverride(name = "mean", column = @Column(name = "ticketResolutionTimeMean")),
            @AttributeOverride(name = "median", column = @Column(name = "ticketResolutionTimeMedian")),
            @AttributeOverride(name = "iqr", column = @Column(name = "ticketResolutionTimeIqr")),
            @AttributeOverride(name = "stdDev", column = @Column(name = "ticketResolutionTimeStdDev"))
    })
    private NumberMetrics ticketResolutionTime;
    @Embedded
    @MetricDescription(displayName = "Percent Of Fields Used")
    @AttributeOverrides({
            @AttributeOverride(name = "mean", column = @Column(name = "percentFieldsUsedMean")),
            @AttributeOverride(name = "median", column = @Column(name = "percentFieldsUsedMedian")),
            @AttributeOverride(name = "iqr", column = @Column(name = "percentFieldsUsedIqr")),
            @AttributeOverride(name = "stdDev", column = @Column(name = "percentFieldsUsedStdDev"))
    })
    private NumberMetrics percentFieldsUsed;

    BugDatabaseMetrics() {
        // Hibernate only
    }

    BugDatabaseMetrics(Collection<BugDatabase> bugDbs) {
        setupAll(bugDbs);
    }

    private void setupAll(Collection<BugDatabase> bugDbs) {
        Collection<Issue> issues = new LinkedList<>();
        for (BugDatabase bd : bugDbs) {
            issues.addAll(bd.getAllIssues().values());
        }

        setupEffortAndResolutionMetrics(issues);
        setupPeopleInDatabase(issues);
        setupByMonthStats(issues);
        setupNumberFields(issues);
        numberReleases = bugDbs.stream()
                .mapToLong(BugDatabase::getNumberReleases)
                .sum();
    }

    void setupNumberFields(Collection<Issue> issues) {
        numberFields = issues.stream()
                .mapToLong(Issue::getFieldsReturned)
                .max()
                .orElse(-1);
        percentFieldsUsed = createNumberMetrics(issues.stream()
                .mapToDouble(Issue::getPercentFieldsUsed)
                .toArray());

        numericFields = new HashMap<>();
        Map<String, List<Double>> nums = new HashMap<>();
        issues.forEach(i -> i.getNumericFields().forEach((key, value) -> {
            if (!nums.containsKey(key)) {
                nums.put(key, new ArrayList<>());
            }
            nums.get(key).add(value);
        }));

        nums.forEach((key, value) -> {
            double[] doubles = new double[value.size()];
            for (int i = 0; i < doubles.length; i++) {
                doubles[i] = value.get(i);
            }
            numericFields.put(key, createNumberMetrics(doubles));
        });
    }

    void setupPeopleInDatabase(Collection<Issue> issues) {
        Set<String> people = new HashSet<>();
        issues.forEach(i -> {
            if (i.getAssigned() != null) {
                people.addAll(i.getAssigned());
            }
            if (i.getReporter() != null) {
                people.add(i.getReporter());
            }
        });
        numberPeopleInDatabase = people.size();
        averageDevelopersPerCompleteTicket = issues.stream()
                .filter(i -> "Resolved".equalsIgnoreCase(i.getStatus()))
                .mapToLong(i -> i.getAssigned().size())
                .average()
                .orElse(-1);
    }

    void setupEffortAndResolutionMetrics(Collection<Issue> issues) {
        Issue earliest = issues.stream()
                .min(Comparator.comparing(Issue::getDateCreated))
                .orElse(null);
        Issue latest = issues.stream()
                .max(Comparator.comparing(Issue::getDateCreated))
                .orElse(null);

        earliestTicket = earliest == null ? null : earliest.getDateCreated();
        lastTicket = latest == null ? null : latest.getDateCreated();
        ticketCount = issues.size();
        ticketsResolved = issues.stream()
                .filter(i -> "Resolved".equalsIgnoreCase(i.getStatus()))
                .count();
        if (ticketCount > 0) {
            setupFieldsRequiringTotalCount(issues);
        }
        if (earliest != null && lastTicket != null) {
            frequencyCompletedTickets = (double) ticketsResolved / (double) ChronoUnit.DAYS.between(earliestTicket, lastTicket);
            monthsTracked = ChronoUnit.MONTHS.between(getEarliestTicket(), ZonedDateTime.now());
        }
    }

    private void setupFieldsRequiringTotalCount(Collection<Issue> issues) {
        percentTicketsEffortSpend = issues.stream()
                .filter(i -> i.getEffortSpent() != null && i.getEffortSpent() > 0)
                .count() / (double) ticketCount;

        percentTicketsEffortEstimate = issues.stream()
                .filter(i -> i.getEffortEstimated() != null && i.getEffortEstimated() > 0)
                .count() / (double) ticketCount;

        percentTicketsEffortEstimateAndSpent = issues.stream()
                .filter(i -> i.getEffortEstimated() != null && i.getEffortEstimated() > 0
                        && i.getEffortSpent() != null && i.getEffortSpent() > 0)
                .count() / (double) ticketCount;

        percentTicketResolved = ticketsResolved / (double) ticketCount;


        fieldAvailability = new HashMap<>();
        issues.forEach(
                i -> i.getFieldsUsed().forEach((key, value) -> {
                    if (!fieldAvailability.containsKey(key)) {
                        fieldAvailability.put(key, 0d);
                    }
                    fieldAvailability.put(key, fieldAvailability.get(key) + (value ? 1 : 0));
                }));
        fieldAvailability.forEach((key, value) -> fieldAvailability.put(key, fieldAvailability.get(key) / ticketCount));
        ticketsTypeBugs = issues.stream()
                .filter(i -> i.getType() == Issue.Type.BUG)
                .count();

        ticketsTypeFeatures = issues.stream()
                .filter(i -> i.getType() == Issue.Type.FEATURE)
                .count();

        ticketsTypeTest = issues.stream()
                .filter(i -> i.getType() == Issue.Type.TEST)
                .count();

    }

    void setupByMonthStats(Collection<Issue> issues) {
        ticketResolutionTime = new NumberMetrics(issues.stream()
                .filter(i -> i.getDateCreated() != null && i.getResolutionDate() != null)
                .mapToDouble(i -> ChronoUnit.MILLIS.between(i.getDateCreated(), i.getResolutionDate()))
                .toArray());
        monthlyTickets = new NumberMetrics(issues.stream()
                .collect(Collectors.groupingByConcurrent(i -> i.getDateCreated().getMonthValue() * i.getDateCreated().getYear(),
                        Collectors.counting())).values().stream().mapToDouble(Long::doubleValue).toArray());
    }
}

