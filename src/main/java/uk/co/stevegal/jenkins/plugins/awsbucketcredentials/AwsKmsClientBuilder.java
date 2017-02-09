package uk.co.stevegal.jenkins.plugins.awsbucketcredentials;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.kms.AWSKMSClient;
import hudson.Util;

import java.io.Serializable;

/**
 * Created by stevegal on 07/02/2017.
 */
public class AwsKmsClientBuilder implements Serializable {
    private static final long serialVersionUID = 1L;

    private String region;
    private String host=null;
    private int port=-1;

    public AWSKMSClient build() {
        ClientConfiguration config = new ClientConfiguration();
        if (!Util.fixNull(host).trim().isEmpty()) {
            config.setProxyHost(this.host);
            config.setProxyPort(this.port);
        }
        AWSKMSClient client = new AWSKMSClient(new DefaultAWSCredentialsProviderChain(), config);
        if (!Util.fixNull(region).trim().isEmpty()) {
            client.setRegion(Region.getRegion(Regions.fromName(region)));
        }
        return client;
    }

    public AwsKmsClientBuilder region(String region) {
        this.region = region;
        return this;
    }

    public AwsKmsClientBuilder proxyHost(String host) {
        this.host = host;
        return this;
    }

    public AwsKmsClientBuilder proxyPort(int port) {
        this.port = port;
        return this;
    }
}
