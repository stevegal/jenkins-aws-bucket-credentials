package uk.co.stevegal.jenkins.plugins.awsbucketcredentials;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import hudson.Util;

import java.io.Serializable;

/**
 * Created by stephengalbraith on 07/02/2017.
 */
public class AwsS3ClientBuilder implements Serializable{
    private static final long serialVersionUID = 1L;

    private String region;
    private String host=null;
    private int port=-1;

    public AmazonS3Client build() {
        ClientConfiguration config = new ClientConfiguration();
        if (!Util.fixNull(host).trim().isEmpty()) {
            config.setProxyHost(this.host);
            config.setProxyPort(this.port);
        }
        AmazonS3Client client = new AmazonS3Client(new DefaultAWSCredentialsProviderChain(), config);
        if (!Util.fixNull(region).trim().isEmpty()) {
            client.setRegion(Region.getRegion(Regions.fromName(region)));
        }
        return client;
    }

    public AwsS3ClientBuilder region(String region) {
        this.region = region;
        return this;
    }

    public AwsS3ClientBuilder proxyHost(String host) {
        this.host = host;
        return this;
    }

    public AwsS3ClientBuilder proxyPort(int port) {
        this.port = port;
        return this;
    }
}
