package edu.calpoly.apacheprojectdata;

import edu.calpoly.apacheprojectdata.data.Project;
import edu.calpoly.apacheprojectdata.metrics.MetricsManager;
import edu.calpoly.apacheprojectdata.web.WebInterface;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Main class used for arg parsing.
 */
public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    private static final String ARG_CLEAN = "c";
    private static final String ARG_CLEAN_LONG = "clean";
    private static final String ARG_DIR = "d";
    private static final String ARG_DIR_LONG = "directory";
    private static final String ARG_PROJECTS = "p";
    private static final String ARG_PROJECTS_LONG = "projects";
    private static final String ARG_APACHE_CONFIG = "a";
    private static final String ARG_APACHE_CONFIG_LONG = "apache";
    private static final String ARG_SNAPSHOT = "s";
    private static final String ARG_SNAPSHOT_LONG = "snapshot";
    static final String DEFAULT_DIR = "../data";
    static final String DEFAULT_APACHE_CONFIG = "../apache.properties";

    private Main(String[] args) throws Exception {
        System.out.println("Start: " + System.currentTimeMillis());
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        Options options = createOptions();
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            new HelpFormatter().printHelp("projects", null, options, null, true);
            System.exit(1);
        }
        Settings.setDataDir(getDataFile(cmd));
        Settings.setApacheConfigFile(getApacheConfigFile(cmd));

        if (cmd.hasOption(ARG_CLEAN)) {
            try {
                FileUtils.cleanDirectory(Settings.getDataDir());
            } catch (IOException e) {
                LOGGER.error("Unable to clean " + Settings.getDataDir().getAbsolutePath(), e);
                System.exit(1);
            }
        }

        Collection<Project> projects = Project.getProjects(getProjects(cmd));
        MetricsManager.createProjectManager(projects);
        if (cmd.hasOption(ARG_SNAPSHOT)) {
            MetricsManager.getInstance().updateAllProjects();
        }
        System.out.println("End: " + System.currentTimeMillis());
        WebInterface web = new WebInterface();
        web.run();
    }

    /**
     * Creates an {@link Options} for the project.
     * @return The CLI {@link Options} for the project.
     */
    private Options createOptions() {
        Options options = new Options();

        options.addOption(
                Option.builder(ARG_DIR)
                        .longOpt(ARG_DIR_LONG)
                        .required(false)
                        .type(String.class)
                        .hasArg()
                        .argName("directory")
                        .desc("The data directory to clone the repositories in. Default " + DEFAULT_DIR)
                        .build());

        options.addOption(
                Option.builder(ARG_PROJECTS)
                        .longOpt(ARG_PROJECTS_LONG)
                        .required(false)
                        .hasArg()
                        .type(String.class)
                        .argName("proj,proj,...")
                        .desc("The keys of the projects to analyze. Check 'https://projects.apache.org/json/foundation/projects.json' to get the keys. If this option is not present, all projects are requested.")
                        .build());

        options.addOption(
                Option.builder(ARG_CLEAN)
                        .longOpt(ARG_CLEAN_LONG)
                        .required(false)
                        .desc("Deletes everything in the data directory.")
                        .build());

        options.addOption(
                Option.builder(ARG_APACHE_CONFIG)
                        .longOpt(ARG_APACHE_CONFIG_LONG)
                        .required(false)
                        .desc("You are required to supply a file for the Apache user credentials. Check the wiki. Default file is '../apache.properties")
                        .build());

        options.addOption(
                Option.builder(ARG_SNAPSHOT)
                        .longOpt(ARG_SNAPSHOT_LONG)
                        .required(false)
                        .desc("Creates a snapshot before starting server. Takes a very long time.")
                        .build());
        return  options;
    }

    /**
     * Gets the data file reference from the CLI args. Defaults to {@link #DEFAULT_DIR}.
     * @param cmd The parsed command line arguments.
     * @return A {@link File} representing the data directory.
     */
    @NotNull
    private File getDataFile(CommandLine cmd) {
        String directoryName = cmd.getOptionValue(ARG_DIR, DEFAULT_DIR);
        File directory = new File(directoryName);

        if (!directory.exists() || !directory.isDirectory()) {
            LOGGER.error("Invalid directory: " + directory.getPath());
        }
        return directory;
    }

    /**
     * Gets the Apache config file reference from the CLI args. Defaults to {@link #DEFAULT_APACHE_CONFIG}.
     * @param cmd The parsed command line arguments.
     * @return A {@link File} representing the Apache config file.
     */
    @NotNull
    private File getApacheConfigFile(CommandLine cmd) {
        String fileName = cmd.getOptionValue(ARG_APACHE_CONFIG, DEFAULT_APACHE_CONFIG);
        File file = new File(fileName);

        if (!file.exists() || !file.isFile()) {
            LOGGER.error("Invalid Apache config file: " + file.getPath());
        }
        return file;
    }

    /**
     * Gets the projects in the command line arguments.
     * @param cmd The parsed command line arguments.
     * @return The number of projects to analyze.
     */
    @NotNull
    private Set<String> getProjects(@NotNull CommandLine cmd) {
        String projects = cmd.getOptionValue(ARG_PROJECTS, "");
        if ("".equals(projects)) {
            return new HashSet<>();
        }
        return new HashSet<>(Arrays.asList(cmd.getOptionValue(ARG_PROJECTS, "").split(",")));
    }

    public static void main(String[] args) throws Exception {
        new Main(args);
    }
}
