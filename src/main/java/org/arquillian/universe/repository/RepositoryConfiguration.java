package org.arquillian.universe.repository;


import java.io.File;

public class RepositoryConfiguration {

    private String remoteForkURL;
    private String remoteUpstreamURL;
    private File baseDir;

    public RepositoryConfiguration(File baseDir, String remoteForkURL, String remoteUpstreamURL) {
        this.baseDir = baseDir;
        this.remoteForkURL = remoteForkURL;
        this.remoteUpstreamURL = remoteUpstreamURL;
    }

    public String getRemoteForkURL() {
        return remoteForkURL;
    }

    public File getBaseDir() {
        return baseDir;
    }

    public String getRemoteUpstreamURL() {
        return remoteUpstreamURL;
    }

}
