package org.arquillian.universe;

import org.arquillian.universe.git.GitClient;
import org.arquillian.universe.github.GitHubService;
import org.arquillian.universe.repository.RepositoryConfiguration;
import org.arquillian.universe.utils.FileUtils;
import org.eclipse.egit.github.core.service.PullRequestService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.jgit.transport.CredentialsProvider;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static org.arquillian.universe.github.GitHubService.getCredentialProvider;
import static org.arquillian.universe.utils.Constants.*;
import static org.arquillian.universe.utils.FileUtils.createTempFilePath;
import static org.arquillian.universe.utils.Utils.isNullOrEmpty;

public class Main {

    public static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws IOException {

        CommandLineChecker checker = new CommandLineChecker();
        checker.loadArgs(args);

        // Get username & token passed as argument.
        String username = checker.getGithubUserName();
        String token = checker.getGithubAuthToken();

        if (!isNullOrEmpty(username) && !isNullOrEmpty(token)) {

            //Configure Github service using token.
            GitHubService gitHubService = configureGitHubService(token);
            RepositoryService repositoryService = gitHubService.getRepositoryService();

            if (!gitHubService.isRepositoryExists(repositoryService, username, REPOSITORY_NAME)) {
                System.exit(1);
            }

            //Get Local repository Configuration.
            final RepositoryConfiguration repositoryConfiguration = getRepositoryConfiguration(username);

            final CredentialsProvider credentialsProvider = getCredentialProvider(username, token);
            final String branchName = GitClient.createBranchName();

            GitClient gitClient = new GitClient();
            gitClient.setCredentialsProvider(credentialsProvider);

            // Git initial operation
            gitClient.cloneRepository(repositoryConfiguration);
            gitClient.addUpStream(UPSTREAM, repositoryConfiguration.getRemoteUpstreamURL());
            gitClient.fetch(UPSTREAM);
            gitClient.checkout("remotes/upstream/master", branchName);

            // Update Pom dependencies to latest.
            updatePomDependenciesToLatestVersion(repositoryConfiguration);

            if (gitClient.isGitDiff()) {
                gitClient.add(FILE_NAME);
                gitClient.commit("Updated dependency to use latest version.");

                gitClient.push(ORIGIN, branchName);

                PullRequestService pullRequestService = gitHubService.getPullRequestService();

                gitHubService.createPullRequest(pullRequestService, MASTER, username + ":" + branchName);
            }
        }
    }

    private static void updatePomDependenciesToLatestVersion(RepositoryConfiguration repositoryConfiguration) throws IOException {

        Map<String, String> currentVersions = FileUtils.load("dependency.yml");
        Map<String, String> latestVersions = MavenResolver.getLatestVersionsOfPomDependency(currentVersions);

        final String filePath = repositoryConfiguration.getBaseDir().toString() + "/" + FILE_NAME;
        final List<String> updatedDependencyVersion = FileUtils.getUpdatedDependencyVersion(filePath, latestVersions);

        FileUtils.updateFile(new File(filePath), updatedDependencyVersion);
    }

    private static GitHubService configureGitHubService(String token) {
        GitHubService gitHubService = new GitHubService();
        gitHubService.setGitHubClient(GitHubService.getClient(token));

        return gitHubService;
    }

    private static RepositoryConfiguration getRepositoryConfiguration(String username) throws IOException {
        final File localPath = createTempFilePath("universe-bom");
        final String remoteForkUrl = GitHubService.getRemoteForkURL(username, REPOSITORY_NAME);

        return new RepositoryConfiguration(localPath, remoteForkUrl, REMOTE_UP_STREAM_URL);
    }


}
