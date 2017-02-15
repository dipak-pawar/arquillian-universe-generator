package org.arquillian.universe.git;

import org.arquillian.universe.repository.RepositoryConfiguration;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.URIish;

import java.io.File;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.List;

import static org.arquillian.universe.Main.LOGGER;

public class GitClient {

    private Git git;
    private CredentialsProvider credentialsProvider;

    public void setCredentialsProvider(CredentialsProvider credentialsProvider) {
        this.credentialsProvider = credentialsProvider;
    }

    public static String createBranchName() {
        LocalDateTime localDateTime = LocalDateTime.now();
        java.sql.Timestamp timestamp = java.sql.Timestamp.valueOf(localDateTime);
        return "update_pom" + timestamp.getTime();
    }

    public void cloneRepository(RepositoryConfiguration repositoryConfiguration) {

        File baseDir = repositoryConfiguration.getBaseDir();

        CloneCommand cloneCommand = Git.cloneRepository();
        cloneCommand.setURI(repositoryConfiguration.getRemoteForkURL());
        cloneCommand.setDirectory(baseDir);

        try {
            this.git = cloneCommand.call();
            LOGGER.info("Cloned repository at:" + baseDir.getAbsolutePath());
        } catch (GitAPIException e) {
            throw new IllegalStateException("problem with clone", e);
        }
    }

    public void push(String remote, String branch) {
        PushCommand pushCommand = this.git.push();

        pushCommand.setRemote(remote);
        pushCommand.setRefSpecs(new RefSpec(branch + ":" + branch));
        pushCommand.setCredentialsProvider(credentialsProvider);

        try {
            pushCommand.call();
            LOGGER.info("Pushing branch: " + branch + "to remote : " + remote);
        } catch (GitAPIException e) {
            throw new IllegalStateException("Unable to push into remote git repository.", e);
        }
    }

    public void add(String filePattern) {
        AddCommand addCommand = this.git.add();

        addCommand.addFilepattern(filePattern);

        try {
            addCommand.call();
        } catch (GitAPIException e) {
            throw new IllegalStateException("Unable to add pattern" + filePattern + " to git.", e);
        }
    }

    public void commit(String message) {
        CommitCommand commitCommand = git.commit();
        commitCommand.setMessage(message);

        try {
            commitCommand.call();
        } catch (GitAPIException e) {
            throw new IllegalStateException("Unable to commit files.", e);
        }

    }

    public void checkout(String fromBranch, String branchName) {
        CheckoutCommand checkout = this.git.checkout();

        checkout.setCreateBranch(true);
        checkout.setName(branchName);
        checkout.setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK);
        checkout.setStartPoint(fromBranch);

        try {
            checkout.call();
            LOGGER.info("Checked out from " + fromBranch + "to " + branchName);
        } catch (GitAPIException e) {
            throw new IllegalStateException("Problem to creating new branch" + branchName + "from" + fromBranch, e);
        }
    }

    public void addUpStream(String name, String upStreamUrl) {
        RemoteAddCommand remote = this.git.remoteAdd();

        remote.setName(name);
        try {
            remote.setUri(new URIish(upStreamUrl));
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Something wrong in URI" + upStreamUrl, e);
        }

        try {
            remote.call();
        } catch (GitAPIException e) {
            throw new IllegalStateException("Problem to adding upstream", e);
        }
    }

    public void fetch(String branchToFetch) {
        FetchCommand fetch = this.git.fetch();

        fetch.setRemote(branchToFetch);

        try {
            fetch.call();
        } catch (GitAPIException e) {
            throw new IllegalStateException("Problem to fetching" + branchToFetch, e);
        }
    }

    void deleteBranches(String... branchName) {
        DeleteBranchCommand delete = this.git.branchDelete();
        delete.setBranchNames(branchName);
        delete.setForce(true);

        try {
            delete.call();
        } catch (GitAPIException e) {
            throw new IllegalStateException("Problem for deleting branches" + branchName, e);
        }
    }

    public boolean isGitDiff() {
        return !gitDiff().isEmpty();
    }

    private List<DiffEntry> gitDiff() {
        DiffCommand diff = this.git.diff();

        List<DiffEntry> diffEntry;
        try {
            diffEntry = diff.call();
        } catch (GitAPIException e) {
            throw new IllegalStateException("Unable to perform git diff operation", e);
        }
        LOGGER.info("git diff:" + diffEntry + "size: " + diffEntry.size());

        return diffEntry;
    }
}
