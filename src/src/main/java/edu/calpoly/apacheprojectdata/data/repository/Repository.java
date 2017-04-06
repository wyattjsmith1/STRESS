package edu.calpoly.apacheprojectdata.data.repository;

import edu.calpoly.apacheprojectdata.data.Project;
import edu.calpoly.apacheprojectdata.data.Revision;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

/**
 * Represents a repository and provides functions to access certain parts.
 */
public abstract class Repository {

    public enum RepoType {
        GIT, SVN
    }

    private static final String GIT_EXTENSION = ".git";
    private static final String SVN_HOST = "svn.apache.org";

    private URI repository;
    private Project project;

    protected Repository(URI uri, Project project) {
        this.repository = uri;
        this.project = project;
    }

    @Nullable
    public static Repository create(String url, Project project) throws URISyntaxException {
        /*
         * For some reason, Apache gives git repositories like:
         * https://git1-us-west.apache.org/repos/asf?p=cassandra.git
         *
         * But git only works when it is formatted like this:
         * https://git1-us-west.apache.org/repos/asf/cassandra.git
         *
         * Also, Apache requires https and will not accept http.
         */
        String formattedUrl = url
                .replace("?p=", "/")
                .replace("http://git-wip-us", "https://git1-us-west")
                .trim();
        URI repository = new URI(formattedUrl);

        if (repository.toString().endsWith(GIT_EXTENSION)) {
            return new GitRepository(repository, project);
        } else if (repository.toString().contains(SVN_HOST)) {
            return new SvnRepository(repository, project);
        }
        return null;
    }

    /**
     * Returns a {@link URI} representing the remote repository.
     * @return The remote repository.
     */
    URI getRemoteRepository() {
        return repository;
    }

    /**
     * Clones the repository into the {@link Repository#getLocalRepository()} folder.
     * @return true on success, false on failure.
     */
    public abstract boolean cloneRepo();

    /**
     * Returns a {@link File} representing the local directory.
     * @return A {@link File} representing the local directory.
     */
    public abstract File getLocalRepository();

    /**
     * Returns the revisions of the repository in reverse order (Oldest to newest).
     * @return The revisions.
     */
    @NotNull
    public abstract Collection<Revision> getRevisions() throws IOException, GitAPIException;

    /**
     * Gets the project associated with the repository. Used by the repository only.
     * @return The {@link Project} associated with the repository.
     */
    protected Project getProject() {
        return project;
    }

    public abstract RepoType getType();

}
