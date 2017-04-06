package edu.calpoly.apacheprojectdata.util;

import edu.calpoly.apacheprojectdata.data.repository.Repository;
import org.eclipse.jgit.annotations.NonNull;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;

/**
 * Basic file utility functions.
 */
public class FileUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);

    public static final String POM = "pom.xml";
    public static final String ANT = "build.xml";

    private FileUtil() {

    }

    @NotNull
    public static Long lineCount(File file) throws IOException {
        LineNumberReader  lnr = new LineNumberReader(new FileReader(file));
        Long result = lnr.skip(Long.MAX_VALUE) + 1;
        lnr.close();
        return result;
    }

    @NotNull
    public static Long safeLineCount(File file) {
        try {
            return FileUtil.lineCount(file);
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to get line count", e);
        }
    }

    @NotNull
    public static Boolean projectUsesMaven(Repository repo) {
        return fileInProject(repo, POM);
    }

    @NotNull
    public static Boolean projectUsesAnt(Repository repo) {
        return fileInProject(repo, ANT);
    }

    @NotNull
    private static Boolean fileInProject(Repository repo, String filename) {
        return getProjectFile(repo, filename).exists();
    }

    @NotNull
    private static File getProjectFile(Repository repo, String filename) {
        return new File(repo.getLocalRepository(), filename);
    }

    @NotNull
    public static File getPom(Repository repo) {
        return getProjectFile(repo, POM);
    }

    @NonNull
    public static Long recursiveFileCount(Repository repo) throws IOException {
        if (repo.getLocalRepository() == null || !repo.getLocalRepository().exists()) {
            return 0L;
        }
        return Files.walk(repo.getLocalRepository().toPath())
                .filter(Files::isRegularFile)
                .filter(f -> {
                    try {
                        return !Files.isHidden(f);
                    } catch (IOException e) {
                        LOGGER.warn("Unable to filter hidden directories", e);
                        return false;
                    }
                })
                .count();
    }

    public static File getTempFile() {
        try{
            return File.createTempFile("data", ".csv");
        } catch (IOException e){
            LOGGER.error("Unable to create temp file", e);
            return null;
        }
    }
}
