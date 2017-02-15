package org.arquillian.universe.github;


import org.apache.commons.lang3.Validate;
import org.arquillian.universe.utils.Constants;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.PullRequestMarker;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.RequestException;
import org.eclipse.egit.github.core.service.PullRequestService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.IOException;

import static org.arquillian.universe.Main.LOGGER;
import static org.arquillian.universe.utils.Constants.ARQUILLIAN;
import static org.arquillian.universe.utils.Constants.REPOSITORY_NAME;

public class GitHubService {

    private GitHubClient gitHubClient;

    public static GitHubClient getClient(String token) {
        GitHubClient client = getClient();
        client.setOAuth2Token(token);

        return client;
    }

    private static GitHubClient getClient() {
        return new GitHubClient();
    }

    public static String getRemoteForkURL(String username, String repositoryName) {

        return "https://github.com/" + username + "/" + repositoryName;
    }

    public static CredentialsProvider getCredentialProvider(String username, String token) {
        return new UsernamePasswordCredentialsProvider(username, token);
    }

    public void setGitHubClient(GitHubClient gitHubClient) {
        this.gitHubClient = gitHubClient;
    }

    public void createPullRequest(PullRequestService pullRequestService, String baseBranch, String headBranch) throws IOException {
        PullRequest pullRequest = new PullRequest();
        pullRequest.setTitle(Constants.PR_TITLE);
        pullRequest.setBody(Constants.PR_BODY);

        PullRequestMarker base = new PullRequestMarker();
        base.setLabel(baseBranch);

        PullRequestMarker head = new PullRequestMarker();
        head.setLabel(headBranch);

        pullRequest.setHead(head);
        pullRequest.setBase(base);

        pullRequestService.createPullRequest(new RepositoryId(ARQUILLIAN, REPOSITORY_NAME), pullRequest);
        LOGGER.info("Created pull request from " + headBranch + " to base branch " + baseBranch);

    }

    public PullRequestService getPullRequestService() {
        Validate.notNull(gitHubClient);

        return new PullRequestService(gitHubClient);
    }

    public RepositoryService getRepositoryService() {
        Validate.notNull(gitHubClient);

        return new RepositoryService(gitHubClient);
    }

    public boolean isRepositoryExists(RepositoryService repositoryService, String username, String repositoryName) throws IOException {
        try {
            Repository repo = repositoryService.getRepository(username, repositoryName);
            if (repositoryName.equals(repo.getName())) {
                LOGGER.info("Found fork : " + repositoryName + " for user : " + username);
                return true;
            }
        } catch (RequestException e) {
            if (404 == e.getStatus()) {
                LOGGER.info("Couldn't find fork " + repositoryName + "for user : " + username);
                return false;
            } else {
                throw e;
            }
        }
        return false;
    }

}
