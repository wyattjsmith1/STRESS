package edu.calpoly.apacheprojectdata.util;

import edu.calpoly.apacheprojectdata.ApacheProjectDataTest;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for {@link StringUtil}.
 */
public class StringUtilTest extends ApacheProjectDataTest {

    @Test
    public void testStringIsEmpty_nullString_returnTrue() {
        assertTrue(StringUtil.stringIsEmpty(null));
    }

    @Test
    public void testStringIsEmpty_stringLength0_returnTrue() {
        assertTrue(StringUtil.stringIsEmpty(""));
    }

    @Test
    public void testStringIsEmpty_stringLengthGt0_returnFalse() {
        new StringUtil(); // Code coverage issue.
        assertFalse(StringUtil.stringIsEmpty("Hello World"));
    }
}