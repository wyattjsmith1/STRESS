package edu.calpoly.apacheprojectdata.data.repository;

import edu.calpoly.apacheprojectdata.data.Project;
import edu.calpoly.apacheprojectdata.data.Revision;
import edu.calpoly.apacheprojectdata.data.bugdatabase.BugDatabase;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a Git repository.
 */
public class GitRepository extends Repository {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitRepository.class);

    public static final String GIT_FILENAME = "git";
    private static final String GIT_DIR = ".git";
    private Collection<Revision> revisions;

    GitRepository(URI uri, Project project) {
        super(uri, project);
    }

    @Override
    public boolean cloneRepo() {
        File local = getLocalRepository();
        try {
            // Get a reference to the Git repo
            Git git;
            boolean result;
            if (!new File(local, GIT_DIR).exists()) {
                if (!local.exists()) {
                    if (!local.mkdirs()) {
                        LOGGER.debug("Can't make directory: " + local.getCanonicalPath());
                        return false;
                    }
                }
                git = Git.cloneRepository().setURI(getRemoteRepository().toString()).setDirectory(local).call();
            } else {
                git = Git.open(new File(local, GIT_DIR));
            }

            // Pull data. If it fails, tell the callback.
            if (git.pull().call().isSuccessful()) {
                result = true;
            } else {
                LOGGER.debug("Failed to pull git repo.");
                result = false;
            }
            git.close();
            return result;
        } catch (GitAPIException e) {
            LOGGER.error("Error cloning repo", e);
            if (local.delete()) {
                LOGGER.error("Failed to delete file.");
            }
            return false;
        } catch (IOException e) {
            LOGGER.error("Error cloning repo", e);
            return false;
        }
    }

    @Override
    public File getLocalRepository() {
        File result = new File(getProject().getProjectDirectory(), GIT_FILENAME);
        if (!result.exists() && !result.mkdirs()) {
            LOGGER.error("Unable to create directories for git repo");
        }
        return result;
    }

    public void updateRevisions() {
        cloneRepo();
        Deque<Revision> gitRevisions = new LinkedList<>();
        Git git = null;
        try {
            git = Git.open(getLocalRepository());
            Iterable<RevCommit> commits = git.log().all().call();

            String[] dbName = getProject().getBugDatabases().stream()
                    .map(BugDatabase::getTicketIdRexEx)
                    .map(String::toLowerCase)
                    .toArray(String[]::new);

            for (RevCommit commit : commits) {
                // Find a commit id in the message and pray to any god you believe in that the developers added it.
                Collection<String> ids = new HashSet<>();
                Pattern pattern = Pattern.compile(String.format("(%s)", String.join("|", dbName)));
                Matcher match = pattern.matcher(commit.getFullMessage().toLowerCase());
                while (match.find()) {
                    ids.add(match.group(1));
                }

                int length;
                if (commit.getParentCount() == 0) {
                    length = 0;
                } else {
                    ObjectReader reader = git.getRepository().newObjectReader();
                    CanonicalTreeParser oldTree = new CanonicalTreeParser(null, reader, commit.getParent(0).getTree());
                    CanonicalTreeParser newTree = new CanonicalTreeParser(null, reader, commit.getTree());
                    length = git.diff().setOldTree(oldTree).setNewTree(newTree).call().size();
                }

                // Commit time in seconds since epoch, ofEpochMilli is in milliseconds.
                ZonedDateTime created = ZonedDateTime.ofInstant(Instant.ofEpochMilli(TimeUnit.SECONDS.toMillis(commit.getCommitTime())), ZoneOffset.UTC);
                Revision revision = new Revision();
                revision.setHash(commit.hashCode());
                revision.setIssueIds(ids);
                revision.setDate(created);
                revision.setAuthor(commit.getAuthorIdent().getName());
                revision.setLength(length);

                gitRevisions.addFirst(revision);
            }
        } catch (IOException | GitAPIException e) {
            LOGGER.error("Unable to read commits", e);
        } finally {
            if (git != null) {
                git.close();
            }
        }
        revisions = gitRevisions;
    }

    @Override
    public RepoType getType() {
        return RepoType.GIT;
    }

    @Override
    @NotNull
    public Collection<Revision> getRevisions() throws IOException, GitAPIException {
        if (revisions == null) {
            updateRevisions();
        }
        return revisions;
    }
}
