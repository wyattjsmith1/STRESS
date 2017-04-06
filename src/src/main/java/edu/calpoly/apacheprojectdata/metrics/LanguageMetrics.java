package edu.calpoly.apacheprojectdata.metrics;

import edu.calpoly.apacheprojectdata.data.Project;
import edu.calpoly.apacheprojectdata.data.repository.Repository;
import edu.calpoly.apacheprojectdata.util.FileUtil;
import lombok.Data;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Represents a single language and its statistics a project can have. We do not know the total number of lines in the
 * project prior to running all of these. Therefore, we just count the lines in this class, sum them in
 * {@link ProjectMetrics}, and set the total later to generate the percentage.
 *
 */
@Data
@Embeddable
public class LanguageMetrics {

    private static final Logger LOGGER = LoggerFactory.getLogger(LanguageMetrics.class);

    protected static final Map<String, String> FILE_TYPE;

    @Column Long linesCount;
    @Column Double linesPercent;

    private LanguageMetrics() {
        // For Hibernate only
    }

    LanguageMetrics(Project project, String language) {
        linesCount = getFileType(project, language).stream()
                .mapToLong(FileUtil::safeLineCount)
                .sum();
    }

    void setTotalLines(Long lines) {
        // Some repos are empty
        if (lines == 0) {
            linesPercent = 0d;
        } else {
            linesPercent = (double) linesCount / lines;
        }
    }

    @NotNull
    private static Collection<File> getFileType(Project project, String type) {
        Collection<File> result = new HashSet<>();
        for (Repository repo : project.getRepositories()) {
            if (repo.getLocalRepository() != null) {
                result.addAll(FileUtils.listFiles(
                        repo.getLocalRepository(),
                        new RegexFileFilter(FILE_TYPE.get(type)),
                        DirectoryFileFilter.DIRECTORY
                ));
            }
        }
        return result;
    }

    static {
        FILE_TYPE = new HashMap<>();
        try {
            InputStream stream = LanguageMetrics.class.getResourceAsStream("/languages.properties");
            Properties props = new Properties();
            props.load(stream);
            Enumeration enumeration = props.propertyNames();
            while (enumeration.hasMoreElements()) {
                String key = (String) enumeration.nextElement();
                FILE_TYPE.put(key, props.getProperty(key));
            }
        } catch (IOException e) {
            LOGGER.error("Unable to read languages.properties.", e);
        } catch (Exception e) {
            LOGGER.error("Error handling languages.properties", e);
        }
    }
}
