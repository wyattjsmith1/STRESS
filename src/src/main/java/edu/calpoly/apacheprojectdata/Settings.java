package edu.calpoly.apacheprojectdata;

import org.eclipse.jgit.annotations.NonNull;
import org.eclipse.jgit.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Holds constants in the program. Not ideal, but some variables will get passed around too much without it.
 */
public class Settings {

    private static final Logger LOGGER = LoggerFactory.getLogger(Settings.class);

    private static File dataDir;
    private static File apacheConfig;

    private Settings() {

    }

    @Nullable
    public static File getDataDir() {
        return dataDir;
    }

    public static void setDataDir(@NonNull File dataDir) {
        try {
            Settings.dataDir = dataDir.getCanonicalFile();
        } catch (IOException e) {
            LOGGER.error("Canonical file not found for data directory.", e);
        }
    }

    @Nullable
    public static File getApacheConfig() {
        return apacheConfig;
    }

    static void setApacheConfigFile(@NonNull File apacheConfig) {
        try {
            Settings.apacheConfig = apacheConfig.getCanonicalFile();
        } catch (IOException e) {
            LOGGER.error("Canonical file not found for Apache.", e);
        }
    }
}
