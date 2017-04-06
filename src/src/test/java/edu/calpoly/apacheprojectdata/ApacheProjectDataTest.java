package edu.calpoly.apacheprojectdata;

import org.junit.Before;

import java.io.File;

/**
 * Superclass for testing.
 */
public class ApacheProjectDataTest {

    @Before
    public void initSettings() {
        Settings.setDataDir(new File(Main.DEFAULT_DIR));
        Settings.setApacheConfigFile(new File(Main.DEFAULT_APACHE_CONFIG));
    }
}
