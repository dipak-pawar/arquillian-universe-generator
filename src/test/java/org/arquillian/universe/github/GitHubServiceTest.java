package org.arquillian.universe.github;


import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.RequestError;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.GitHubResponse;
import org.eclipse.egit.github.core.client.RequestException;
import org.eclipse.egit.github.core.service.PullRequestService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;

import static org.arquillian.universe.utils.Constants.MASTER;
import static org.arquillian.universe.utils.Constants.REPOSITORY_NAME;
import static org.arquillian.universe.utils.Constants.USER_NAME;
import static org.arquillian.universe.utils.Constants.PR_TITLE;
import static org.arquillian.universe.utils.Constants.PR_BODY;
import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class GitHubServiceTest {

    @Rule
    public ExpectedException thown = ExpectedException.none();

    @Captor
    private ArgumentCaptor<PullRequest> argumentCaptor = ArgumentCaptor.forClass(PullRequest.class);

    @Mock
    private GitHubClient client;

    @Mock
    private RepositoryService repositoryService;

    @Mock
    private PullRequestService pullRequestService;

    @Mock
    private GitHubResponse response;

    @Test
    public void should_have_repository_exists() throws IOException {
        //given
        Repository repository = new Repository();
        repository.setName(REPOSITORY_NAME);

        doReturn(repository).when(repositoryService).getRepository(USER_NAME, REPOSITORY_NAME);
        GitHubService gitHubService = createGitHubService();

        //when
        final boolean repositoryExists = gitHubService.isRepositoryExists(repositoryService, USER_NAME, REPOSITORY_NAME);

        //then
        assertThat(repositoryExists).isTrue();
    }

    @Test
    public void should_not_have_repository_exists_if_name_different() throws IOException {
        //given
        Repository repository = new Repository();
        repository.setName("repository");

        doReturn(repository).when(repositoryService).getRepository(USER_NAME, REPOSITORY_NAME);
        GitHubService gitHubService = createGitHubService();

        //when
        final boolean repositoryExists = gitHubService.isRepositoryExists(repositoryService, USER_NAME, REPOSITORY_NAME);

        //then
        assertThat(repositoryExists).isFalse();
    }

    @Test
    public void should_get_repository_exists_for_request_exception_with_404() throws IOException {
        //given
        doThrow(new RequestException(new RequestError(), 404)).when(repositoryService).getRepository(USER_NAME, REPOSITORY_NAME);
        GitHubService gitHubService = createGitHubService();

        //when
        final boolean repositoryExists = gitHubService.isRepositoryExists(repositoryService, USER_NAME, REPOSITORY_NAME);

        //then
        assertThat(repositoryExists).isFalse();
    }

    @Test
    public void should_thow_exception_for_request_exception_with_500() throws IOException {

        doThrow(new RequestException(new RequestError(), 500)).when(repositoryService).getRepository(USER_NAME, REPOSITORY_NAME);
        GitHubService gitHubService = createGitHubService();

        thown.expect(RequestException.class);

        gitHubService.isRepositoryExists(repositoryService, USER_NAME, REPOSITORY_NAME);

    }

    @Test
    public void should_create_pull_request() throws IOException {
        //given
        GitHubService gitHubService = createGitHubService();

        //when
        gitHubService.createPullRequest(pullRequestService, USER_NAME + ":base", MASTER);

        //then
        verify(pullRequestService).createPullRequest(any(RepositoryId.class), argumentCaptor.capture());
        PullRequest pullRequest = argumentCaptor.getValue();

        assertThat(pullRequest).isNotNull();
        assertThat(USER_NAME + ":base").isEqualTo(pullRequest.getBase().getLabel());
        assertThat(MASTER).isEqualTo(pullRequest.getHead().getLabel());
        assertThat(PR_TITLE).isEqualTo(pullRequest.getTitle());
        assertThat(PR_BODY).isEqualTo(pullRequest.getBody());
    }

    private GitHubService createGitHubService() throws IOException {
        GitHubService gitHubService = new GitHubService();
        gitHubService.setGitHubClient(client);

        return gitHubService;
    }
}