package edu.calpoly.apacheprojectdata.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.jgit.annotations.Nullable;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Set;

/**
 * Represents a single issue in a bug database.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Issue {

    public enum Type {
        BUG, TEST, FEATURE, OTHER
    }

    private String id;
    private String reporter;
    private Set<String> assigned;
    private Integer effortSpent;
    private Integer effortEstimated;
    private String status;
    private Type type;
    private Integer fieldsReturned;
    private Double percentFieldsUsed;
    private Map<String, Boolean> fieldsUsed;
    private Map<String, Double> numericFields;
    private ZonedDateTime dateCreated;

    @Nullable
    private ZonedDateTime resolutionDate;
}
