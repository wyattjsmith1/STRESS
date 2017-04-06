package edu.calpoly.apacheprojectdata.util;

import edu.calpoly.apacheprojectdata.ApacheProjectDataTest;
import org.junit.Test;

import javax.sql.rowset.spi.SyncFactoryException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Tests a
 */
public class CollectionUtilTest extends ApacheProjectDataTest {

    @Test
    public void testIterableSize_list_returnSize() {
        new CollectionUtil(); // Code coverage. Because I seek perfection.
        List<Integer> list = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        assertEquals(10, CollectionUtil.iterableSize(list));
    }

    @Test
    public void testIterableSize_sizeless_returnSize() {
        assertEquals(1, CollectionUtil.iterableSize(new SyncFactoryException()));
    }
}