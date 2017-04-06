package edu.calpoly.apacheprojectdata.metrics;

/**
 * Base class for metrics generation.
 */
public abstract class Metrics {

    /**
     * Creates a {@link NumberMetrics}. Used for testing.
     * @return A number metrics for the data.
     */
    protected NumberMetrics createNumberMetrics(double[] data) {
        return new NumberMetrics(data);
    }
}
