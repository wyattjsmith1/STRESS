package edu.calpoly.apacheprojectdata.web;

import lombok.Data;

import java.util.List;

/**
 * Holds data for the Sample
 */
@Data
public class SampleContainer {
    private List<Integer> projects;
    private List<String> fields;
    private Integer numberSamples;
}
