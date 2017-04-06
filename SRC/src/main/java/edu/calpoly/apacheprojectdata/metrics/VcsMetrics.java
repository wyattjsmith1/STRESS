package edu.calpoly.apacheprojectdata.metrics;

import edu.calpoly.apacheprojectdata.data.Revision;
import edu.calpoly.apacheprojectdata.data.repository.Repository;
import edu.calpoly.apacheprojectdata.util.FileUtil;
import lombok.Getter;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * Holder for VCS metrics.
 */
@Getter
class VcsMetrics extends Metrics {

    private static final Logger LOGGER = LoggerFactory.getLogger(VcsMetrics.class);

    @MetricDescription(displayName = "Earliest Commit Date", group = MetricDescription.Group.ACTIVITY)
    @Column private ZonedDateTime earliestCommit;

    @MetricDescription(displayName = "Last Commit Date", group = MetricDescription.Group.ACTIVITY)
    @Column private ZonedDateTime lastCommit;

    @MetricDescription(displayName = "Number Of Commits", group = MetricDescription.Group.ACTIVITY)
    @Column private Integer commitCount;

    @MetricDescription(displayName = "Number Of Authors", group = MetricDescription.Group.ACTIVITY)
    @Column private Long numberAuthors;

    @MetricDescription(displayName = "Number Of Files", group = MetricDescription.Group.SIZE)
    @Column private Long numberFiles;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "mean", column = @Column(name = "monthlyCommitsMean")),
            @AttributeOverride(name = "median", column = @Column(name = "monthlyCommitsMedian")),
            @AttributeOverride(name = "iqr", column = @Column(name = "monthlyCommitsIqr")),
            @AttributeOverride(name = "stdDev", column = @Column(name = "monthlyCommitsStdDev"))
    })
    @MetricDescription(displayName = "Monthly Commits")
    private NumberMetrics monthlyCommits;
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "mean", column = @Column(name = "commitLengthMean")),
            @AttributeOverride(name = "median", column = @Column(name = "commitLengthMedian")),
            @AttributeOverride(name = "iqr", column = @Column(name = "commitLengthIqr")),
            @AttributeOverride(name = "stdDev", column = @Column(name = "commitLengthStdDev"))
    })
    @MetricDescription(displayName = "Commit Length")
    private NumberMetrics commitLength;

    VcsMetrics() {
        // Hibernate and testing
    }

    VcsMetrics(Collection<Repository> repos) throws IOException, GitAPIException {
        setupAll(repos);
    }

    void setupAll(Collection<Repository> repos) throws IOException, GitAPIException {
        Collection<Revision> revisions = new LinkedList<>();
        for (Repository repo : repos) {
            revisions.addAll(repo.getRevisions());
        }

        setupAuthors(revisions);
        setupFirstLast(revisions);
        setupCommitCounts(revisions);
        setupFileCount(repos);
    }

    void setupFileCount(Collection<Repository> repos) {
        numberFiles = repos.stream().mapToLong(r -> {
            try {
                return FileUtil.recursiveFileCount(r);
            } catch (IOException e) {
                LOGGER.error("Unable to get tho file count", e);
            }
            return 0;
        }).sum();
    }

    void setupCommitCounts(Collection<Revision> revisions) throws IOException, GitAPIException {
        commitCount = revisions.size();
        monthlyCommits = createNumberMetrics(revisions.stream()
                .collect(Collectors.groupingByConcurrent(r -> r.getDate().getMonthValue() * r.getDate().getYear(),
                        Collectors.counting())).values().stream().mapToDouble(Long::doubleValue).toArray());
        commitLength = createNumberMetrics(revisions.stream()
                .mapToDouble(Revision::getLength)
                .toArray());

    }

    void setupAuthors(Collection<Revision> revisions) throws IOException, GitAPIException {
        numberAuthors = revisions.stream()
                .map(Revision::getAuthor)
                .distinct()
                .count();
    }

    void setupFirstLast(Collection<Revision> revisions) throws IOException, GitAPIException {
        Revision earliest = revisions.stream()
                .min(Comparator.comparing(Revision::getDate))
                .orElse(null);
        Revision last = revisions.stream()
                .max(Comparator.comparing(Revision::getDate))
                .orElse(null);

        if (earliest != null) {
            earliestCommit = earliest.getDate();
        }
        if (last != null) {
            lastCommit = last.getDate();
        }
    }
}
