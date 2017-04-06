package edu.calpoly.apacheprojectdata.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.Collection;

/**
 * Represents a single commit in a repository.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Revision {

    private Integer hash;
    private Collection<String> issueIds;
    private String author;
    private Integer length;
    private ZonedDateTime date;
}
