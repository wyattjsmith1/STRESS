package edu.calpoly.apacheprojectdata.metrics;

import lombok.Data;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Represents the Mean, Median, Standard Deviation, and IQR.
 */
@Data
@Embeddable
public class NumberMetrics {

    private static final int Q1 = 25;
    private static final int Q2 = 50;
    private static final int Q3 = 75;

    @MetricDescription(displayName = "Mean", group = MetricDescription.Group.SIZE)
    @Column private Double mean;
    @MetricDescription(displayName = "Median", group = MetricDescription.Group.SIZE)
    @Column private Double median;
    @MetricDescription(displayName = "IQR", group = MetricDescription.Group.SIZE)
    @Column private Double iqr;
    @MetricDescription(displayName = "Standard Deviation", group = MetricDescription.Group.SIZE)
    @Column private Double stdDev;

    private NumberMetrics() {
        // Hibernate
    }

    public NumberMetrics(double[] numbers) {
        if (numbers.length > 0) {
            DescriptiveStatistics statistics = new DescriptiveStatistics(numbers);
            mean = statistics.getMean();
            median = statistics.getPercentile(Q2);
            iqr = statistics.getPercentile(Q3) - statistics.getPercentile(Q1);
            stdDev = statistics.getStandardDeviation();
        }
    }
}
