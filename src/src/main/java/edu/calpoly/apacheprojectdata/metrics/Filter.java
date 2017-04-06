package edu.calpoly.apacheprojectdata.metrics;

import lombok.Data;

/**
 * A search filter.
 */
@Data
public class Filter {

    private String field;
    private String comparator;
    private String value;
}
