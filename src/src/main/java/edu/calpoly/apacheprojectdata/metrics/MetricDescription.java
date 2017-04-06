package edu.calpoly.apacheprojectdata.metrics;

import com.fasterxml.jackson.annotation.JsonValue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * describes a metric
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface MetricDescription {

    enum Group {
        GENERAL("General"), TECH("Tech"), SIZE("Size"), ACTIVITY("Activity"), LANGUAGE("Language");

        String text;

        Group(String text) {
            this.text = text;
        }

        @Override
        @JsonValue
        public String toString() {
            return text;
        }
    }
    String displayName() default "";
    Group group() default Group.GENERAL;
}
