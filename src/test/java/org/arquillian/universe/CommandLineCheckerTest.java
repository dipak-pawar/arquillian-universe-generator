package org.arquillian.universe;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.arquillian.universe.utils.Constants;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

import java.util.Collection;

import static org.arquillian.universe.utils.Constants.AUTH_TOKEN;
import static org.arquillian.universe.utils.Constants.AUTH_TOKEN_DESC;
import static org.arquillian.universe.utils.Constants.USER_NAME_DESC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class CommandLineCheckerTest {

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Test
    public void should_get_options_after_instantiation() {
        //given
        CommandLineChecker commandLineChecker = new CommandLineChecker();

        //when
        Options options = CommandLineChecker.options;

        //then
        assertThat(options).isNotNull();
        Collection<Option> optionCollection = options.getOptions();

        assertThat(optionCollection).hasSize(2);
        assertThat(optionCollection).extracting("opt", "description", "numberOfArgs").contains(
                tuple("t", AUTH_TOKEN_DESC, 1),
                tuple("u", USER_NAME_DESC, 1));
    }

    @Test
    public void should_load_args() {
        //given
        CommandLineChecker commandLineChecker = new CommandLineChecker();
        String[] args = {"-t foo", "-u bar"};

        //when
        commandLineChecker.loadArgs(args);

        //then
        String userName = commandLineChecker.getGithubUserName();
        assertThat(userName).isNotNull();
        assertThat(userName).isEqualTo("bar");

        String token = commandLineChecker.getGithubAuthToken();
        assertThat(token).isNotNull();
        assertThat(token).isEqualTo("foo");

    }

    @Test
    public void should_exit_program_if_token_is_missing() {

        CommandLineChecker commandLineChecker = new CommandLineChecker();
        String[] args = {"-u bar"};

        // Checked this if user has already set AUTH_TOKEN as env variable.
        if (System.getenv(AUTH_TOKEN) == null) {
            exit.expectSystemExit();
            exit.expectSystemExitWithStatus(0);
        }

        commandLineChecker.loadArgs(args);

    }

    @Test
    public void should_exit_program_if_user_is_missing() {
        CommandLineChecker commandLineChecker = new CommandLineChecker();
        String[] args = {"-t foo"};

        exit.expectSystemExit();
        exit.expectSystemExitWithStatus(0);

        commandLineChecker.loadArgs(args);

    }

    @Test
    public void should_load_args_with_user_as_arg_and_token_as_env_varibale() {
        //given
        environmentVariables.set(Constants.AUTH_TOKEN, "foo");
        CommandLineChecker commandLineChecker = new CommandLineChecker();
        String[] args = {"-u bar"};

        //when
        commandLineChecker.loadArgs(args);

        //then
        String userName = commandLineChecker.getGithubUserName();
        assertThat(userName).isNotNull();
        assertThat(userName).isEqualTo("bar");

        String token = commandLineChecker.getGithubAuthToken();
        assertThat(token).isNotNull();
        assertThat(token).isEqualTo("foo");

    }

    @Test
    public void should_exit_if_no_args() {

        CommandLineChecker commandLineChecker = new CommandLineChecker();
        String[] args = {""};

        exit.expectSystemExit();
        exit.expectSystemExitWithStatus(0);

        commandLineChecker.loadArgs(args);

    }

}