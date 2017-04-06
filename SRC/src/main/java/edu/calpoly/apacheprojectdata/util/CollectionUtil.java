package edu.calpoly.apacheprojectdata.util;

/**
 * Random utility functions for collections.
 */
public class CollectionUtil {

    CollectionUtil() {

    }

    /**
     * Calculating the size of an iterable is interesting. It may come with a size, but if it doesn't you need to iterate and count. This method checks both.
     * @param iterable The iterable to check the size for.
     * @return the size of the iterable.
     */
    public static long iterableSize(Iterable iterable) {
        long size = iterable.spliterator().getExactSizeIfKnown();
        if (size == -1) {
            size = 0;
            for (Object ignored : iterable) {
                size++;
            }
        }
        return size;
    }
}
