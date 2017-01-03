package org.arquillian.universe;


import org.apache.commons.cli.*;
import org.arquillian.universe.utils.Constants;

import static org.arquillian.universe.utils.Constants.AUTH_TOKEN_DESC;
import static org.arquillian.universe.utils.Constants.USER_NAME_DESC;

class CommandLineChecker {

    private static final String USER_NAME = "u";
    private static final String AUTH_TOKEN = "t";
    static Options options = null; // Command line options

    static {
        options = new Options();
        options.addOption(USER_NAME, true, USER_NAME_DESC);
        options.addOption(AUTH_TOKEN, true, AUTH_TOKEN_DESC);
    }

    private String githubUserName;
    private String githubAuthToken;

    String getGithubUserName() {
        return githubUserName;
    }

    String getGithubAuthToken() {
        return githubAuthToken;
    }

    void loadArgs(String[] args) {
        CommandLineParser parser = new PosixParser();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            throw new IllegalArgumentException("problem while parsing options", e);
        }

        if (cmd != null) {
            validateUserName(cmd);
            validateGitHubAuthToken(cmd);
        }
    }

    private void validateUserName(CommandLine cmd) {
        if (!cmd.hasOption(USER_NAME)) {
            displayHelp();
        } else {
            githubUserName = cmd.getOptionValue(USER_NAME).trim();
        }
    }

    private void validateGitHubAuthToken(CommandLine cmd) {
        if (!cmd.hasOption(AUTH_TOKEN)) {
            String authToken = System.getenv(Constants.AUTH_TOKEN);
            if (authToken != null) {
                githubAuthToken = authToken;
            } else {
                displayHelp();
            }
        } else {
            githubAuthToken = cmd.getOptionValue("t").trim();
        }
    }

    private void displayHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java -jar update_pom.jar", options);
        System.exit(0);
    }


}
