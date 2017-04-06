package edu.calpoly.apacheprojectdata.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.Contract;

/**
 * Utility functions for {@link String}.
 */
public class StringUtil {

    public static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.findAndRegisterModules();
    }

    StringUtil() {

    }

    /**
     * Checks if a strung is empty. 'empty' means a length of 0 or null.
     * @param string The string to check
     * @return true if the string is empty, false otherwise.
     */
    @Contract(value = "null -> true", pure = true)
    public static boolean stringIsEmpty(String string) {
        return string == null || string.isEmpty();
    }
}
