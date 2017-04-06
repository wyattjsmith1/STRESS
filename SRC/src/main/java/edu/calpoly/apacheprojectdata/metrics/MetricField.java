package edu.calpoly.apacheprojectdata.metrics;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;

/**
 * Represents a field in the http request.
 */
@Data
public class MetricField {

    public enum DataType {
        NUMBER("number"), DATE("datetime-local"), STRING("text"), BOOLEAN("checkbox");

        String field;

        DataType(String field) {
            this.field = field;
        }

        @Override
        @JsonValue
        public String toString() {
            return field;
        }
    }

    private String fieldName;
    private DataType type;
    private String displayName;
}
