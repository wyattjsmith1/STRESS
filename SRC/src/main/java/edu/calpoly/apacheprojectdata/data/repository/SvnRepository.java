package edu.calpoly.apacheprojectdata.data.repository;

import edu.calpoly.apacheprojectdata.data.Project;
import edu.calpoly.apacheprojectdata.data.Revision;
import edu.calpoly.apacheprojectdata.data.bugdatabase.BugDatabase;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc2.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.tmatesoft.svn.core.wc2.SvnTarget.fromFile;

/**
 * Represents the commands for a SVN Repo. Not yet implemented for real.
 */
public class SvnRepository extends Repository {

    private static final Logger LOGGER = LoggerFactory.getLogger(SvnRepository.class);
    private static final String SVN_FILENAME = "svn";
    private static final String SVN_DIR = ".svn";

    private Collection<Revision> revisions;

    SvnRepository(URI uri, Project project) {
        super(uri, project);
    }

    @Override
    public boolean cloneRepo() {

        try {
            update();
            return true;
        } catch (SVNException e) {
            LOGGER.warn("Unable to checkout SVN repo " + getRemoteRepository() + ". Cleaning repo ant retrying", e);
            try {
                FileUtils.cleanDirectory(getLocalRepository());
                update();
                return true;
            } catch (IOException | SVNException e2) {
                LOGGER.error("Failed to clone svn repo second time. Not retrying anymore.", e2);
                return false;
            }
        }
    }

    private void update() throws SVNException {
        AbstractSvnUpdate action;
        SvnOperationFactory operationFactory = new SvnOperationFactory();
        SvnUpdate update = operationFactory.createUpdate();
        SvnCheckout checkout  = operationFactory.createCheckout();
        if (new File(getLocalRepository(), SVN_DIR).exists()) {
            SvnCleanup cleanup = operationFactory.createCleanup();
            cleanup.setDepth(SVNDepth.INFINITY);
            cleanup.setSingleTarget(SvnTarget.fromFile(getLocalRepository()));
            cleanup.run();
            action = update;
        } else {
            action = checkout;
            checkout.setSource(SvnTarget.fromURL(SVNURL.parseURIEncoded(getRemoteRepository().toString())));
        }

        action.setDepth(SVNDepth.INFINITY);
        action.setSingleTarget(SvnTarget.fromFile(getLocalRepository()));
        action.setRevision(SVNRevision.HEAD);
        action.run();
    }

    @Override
    public File getLocalRepository() {
        File result = new File(getProject().getProjectDirectory(), SVN_FILENAME);
        if (!result.exists() && !result.mkdirs()) {
            LOGGER.error("Unable to create directories for git repo");
        }
        return result;
    }

    @NotNull
    @Override
    public Collection<Revision> getRevisions() throws IOException, GitAPIException {
        if (revisions == null) {
            updateRevisions();
        }
        return revisions;
    }

    private void updateRevisions() throws IOException, GitAPIException {
        revisions = new HashSet<>();
        String[] dbName = getProject().getBugDatabases().stream()
                .map(BugDatabase::getTicketIdRexEx)
                .map(String::toLowerCase)
                .toArray(String[]::new);
        try {
            SvnOperationFactory operationFactory = new SvnOperationFactory();
            SvnLog logOperation = operationFactory.createLog();
            logOperation.setSingleTarget(fromFile(getLocalRepository()));
            logOperation.setRevisionRanges(Collections.singleton(SvnRevisionRange.create(SVNRevision.create(1), SVNRevision.HEAD)));
            logOperation.setDiscoverChangedPaths(true);
            logOperation.run(null).forEach(log -> {
                        Collection<String> ids = new HashSet<>();
                        Pattern pattern = Pattern.compile(String.format("(%s)", String.join("|", dbName)));
                        Matcher match = pattern.matcher(log.getMessage().toLowerCase());
                        while (match.find()) {
                            ids.add(match.group(1));
                        }
                        Revision revision = new Revision();
                        revision.setAuthor(log.getAuthor());
                        revision.setHash(log.hashCode());
                        revision.setDate(ZonedDateTime.ofInstant(Instant.ofEpochMilli(log.getDate().getTime()), ZoneOffset.UTC));
                        revision.setIssueIds(ids);
                        revision.setLength(log.getChangedPaths().size());
                        revisions.add(revision);
                    }
            );
        } catch (SVNException e) {
            LOGGER.warn("Unable to get revision history", e);
        }
    }

    @Override
    public RepoType getType() {
        return RepoType.SVN;
    }
}
