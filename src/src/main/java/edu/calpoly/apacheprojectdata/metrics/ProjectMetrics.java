package edu.calpoly.apacheprojectdata.metrics;

import com.fasterxml.jackson.annotation.JsonIgnore;
import edu.calpoly.apacheprojectdata.data.Issue;
import edu.calpoly.apacheprojectdata.data.Project;
import edu.calpoly.apacheprojectdata.data.Revision;
import edu.calpoly.apacheprojectdata.data.bugdatabase.BugDatabase;
import edu.calpoly.apacheprojectdata.data.repository.Repository;
import edu.calpoly.apacheprojectdata.util.FileUtil;
import lombok.Getter;
import lombok.ToString;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Generates the metrics for the project.
 */
@Entity
@Table
@Getter
@ToString
public class ProjectMetrics {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectMetrics.class);

    @Id
    @GeneratedValue
    @Column(name = "id")
    Long id;

    @MetricDescription(displayName = "Project Homepage")
    @Column private String homepage;

    @MetricDescription(displayName = "Number Commits With Ticket References", group = MetricDescription.Group.SIZE)
    @Column private Long vcsMatchesTicket;

    @MetricDescription(displayName = "Linkage", group = MetricDescription.Group.ACTIVITY)
    @Column private Double percentTicketsReferencedInVcs;

    @MetricDescription(displayName = "Number Of Programming Languages", group = MetricDescription.Group.TECH)
    @Column private Long numberProgrammingLanguages;

    @MetricDescription(displayName = "Lines Of Code", group = MetricDescription.Group.TECH)
    @Column private Long linesOfCode;

    @MetricDescription(displayName = "Project Name")
    @Column private String projectName;

    @MetricDescription(displayName = "Number Git Repos", group = MetricDescription.Group.SIZE)
    @Column private Long numberGitRepos;

    @MetricDescription(displayName = "Number Svn Repos", group = MetricDescription.Group.SIZE)
    @Column private Long numberSvnRepos;

    @MetricDescription(displayName = "Uses Maven?", group = MetricDescription.Group.TECH)
    @Column private Boolean usesMaven;

    @MetricDescription(displayName = "Uses Ant?", group = MetricDescription.Group.TECH)
    @Column private Boolean usesAnt;

    @MetricDescription(displayName = "Number Jira Databases", group = MetricDescription.Group.SIZE)
    @Column private Long numberJiraDatabases;

    @MetricDescription(displayName = "Number Bugzilla Databases", group = MetricDescription.Group.SIZE)
    @Column private Long numberBugzillaDatabases;

    @ElementCollection(targetClass=String.class)
    @Column private Set<String> libraries;

    @Embedded
    @MetricDescription
    private VcsMetrics vcs;

    @Embedded
    @MetricDescription
    private BugDatabaseMetrics bugDatabase;

    @ElementCollection
    @MetricDescription
    private Map<String, LanguageMetrics> languages;

    @JsonIgnore
    @ManyToOne
    @JoinColumn
    private Snapshot snapshot;

    ProjectMetrics() {
        // Hibernate and testing only
    }

    /**
     * Creates a new set of metrics from a project. All network operations are multithreaded.
     * @param project The project to create the metrics for.
     */
    ProjectMetrics(Project project, Snapshot snapshot) throws IOException, GitAPIException {
        this.snapshot = snapshot;
        final boolean[] valid = {true};
        project.getRepositories().forEach(r -> {
            if (valid[0]) {
                valid[0] &= r.cloneRepo();
            }
        });
        project.getBugDatabases().forEach(b -> {
            if (valid[0]) {
                valid[0] &= b.updateIssues();
            }
        });
        if (!valid[0]) {
            return;
        }
        setupAll(project);
    }

    /**
     * Runs all setup. For testing only, otherwise should be in the constructor.
     * @param project The project to setup for.
     */
    void setupAll(Project project) throws IOException, GitAPIException {
        setupProjectFields(project);
        setupLanguages(project);
        setupBuilds(project);

        Collection<Revision> revisions = new LinkedList<>();
        for (Repository repo : project.getRepositories()) {
            revisions.addAll(repo.getRevisions());
        }

        Collection<Issue> issues = new LinkedList<>();
        for (BugDatabase bd : project.getBugDatabases()) {
            issues.addAll(bd.getAllIssues().values());
        }

        vcs = new VcsMetrics(project.getRepositories());
        bugDatabase = new BugDatabaseMetrics(project.getBugDatabases());
        setupLinkage(revisions, issues);
        projectName = project.getName();
    }

    void setupProjectFields(Project project) {
        homepage = project.getHomepage();
        numberGitRepos = project.getRepositories().stream()
                .filter(r -> r.getType() == Repository.RepoType.GIT)
                .count();
        numberSvnRepos = project.getRepositories().stream()
                .filter(r -> r.getType() == Repository.RepoType.SVN)
                .count();
        numberJiraDatabases = project.getBugDatabases().stream()
                .filter(b -> b.getType() == BugDatabase.BugDatabaseType.JIRA)
                .count();
        numberBugzillaDatabases = project.getBugDatabases().stream()
                .filter(b -> b.getType() == BugDatabase.BugDatabaseType.BUGZILLA)
                .count();
    }

    void setupLinkage(Collection<Revision> revisions, Collection<Issue> issues) throws IOException, GitAPIException {
        Set<String> ticketsReferenced = new HashSet<>();
        vcsMatchesTicket = (long) 0;
        revisions.forEach(x -> {
            vcsMatchesTicket += x.getIssueIds().size();
            ticketsReferenced.addAll(x.getIssueIds());
        });

        if (!issues.isEmpty()) {
            percentTicketsReferencedInVcs = ticketsReferenced.size() / (double) issues.size();
        }
    }

    void setupLanguages(Project project) {
        languages = new HashMap<>();
        for (String l : LanguageMetrics.FILE_TYPE.keySet()) {
            languages.put(l, new LanguageMetrics(project, l));
        }

        linesOfCode = languages.values().stream()
                .mapToLong(LanguageMetrics::getLinesCount)
                .sum();
        // Calculate the percentage.
        languages.values().forEach(x -> x.setTotalLines(linesOfCode));
        numberProgrammingLanguages = languages.values().stream()
                .filter(i -> i.getLinesCount() > 0)
                .count();

    }

    void setupBuilds(Project project) {
        libraries = new HashSet<>();
        usesMaven = false;
        usesAnt = false;
        for (Repository repo : project.getRepositories()) {
            if (FileUtil.projectUsesMaven(repo)) {
                usesMaven = true;
                libraries.addAll(parsePom(repo));
            }
            usesAnt |= FileUtil.projectUsesAnt(repo);
        }
    }

    private Collection<String> parsePom(Repository repo) {
        try {
            MavenXpp3Reader reader = new MavenXpp3Reader();
            Model model = reader.read(new FileReader(FileUtil.getPom(repo)));
            List<Dependency> repoDependencies = model.getDependencies();
            return repoDependencies.stream()
                    .map(d -> String.format("%s:%s:%s", d.getArtifactId(), d.getGroupId(), d.getVersion()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            LOGGER.warn("Unable to parse pom.xml", e);
        }
        return Collections.emptyList();
    }
}
