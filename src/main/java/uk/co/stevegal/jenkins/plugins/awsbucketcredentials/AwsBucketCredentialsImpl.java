package uk.co.stevegal.jenkins.plugins.awsbucketcredentials;

import com.amazonaws.services.kms.model.DecryptRequest;
import com.amazonaws.services.kms.model.DecryptResult;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.cloudbees.plugins.credentials.CredentialsDescriptor;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.util.Secret;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.logging.Logger;


/**
 * Created by stevegal on 05/02/2017.
 */
public class AwsBucketCredentialsImpl extends BaseStandardCredentials implements AwsBucketCredentials, StandardUsernamePasswordCredentials {
    private static final long serialVersionUID = 1L;

    private final String bucketName;
    private final String bucketPath;
    private boolean s3Proxy;
    private String kmsEncryptionContextKey;
    private final String kmsSecretName;

    private String username;
    private AwsS3ClientBuilder amazonS3ClientBuilder;
    private AwsKmsClientBuilder amazonKmsClientBuilder;
    private String region;
    private boolean kmsProxy;
    private String proxyHost;
    private String proxyPort;


    private static final Logger LOGGER = Logger.getLogger(AwsBucketCredentialsImpl.class.getName());

    @DataBoundConstructor
    public AwsBucketCredentialsImpl(@CheckForNull CredentialsScope scope, @CheckForNull String id, @CheckForNull String region,
                                    @CheckForNull String bucketName, @CheckForNull String bucketPath,
                                    @CheckForNull String username, @CheckForNull boolean s3Proxy, @CheckForNull String description,
                                    String kmsEncryptionContextKey,  String kmsSecretName, boolean kmsProxy,
                                    String proxyHost, String proxyPort) {
        super(scope, id, description);
        this.bucketName = bucketName;
        this.bucketPath = bucketPath;
        this.s3Proxy = s3Proxy;
        this.kmsEncryptionContextKey = kmsEncryptionContextKey;
        this.kmsSecretName = kmsSecretName;
        this.username = username;
        this.region=region;
        this.kmsProxy = kmsProxy;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.amazonS3ClientBuilder = new AwsS3ClientBuilder();
        this.amazonS3ClientBuilder.region(region);
        if (s3Proxy) {
            this.amazonS3ClientBuilder.proxyHost(proxyHost).proxyPort(Integer.parseInt(proxyPort));
        }
        this.amazonKmsClientBuilder = new AwsKmsClientBuilder();
        this.amazonKmsClientBuilder.region(region);
        if (kmsProxy) {
            this.amazonKmsClientBuilder.proxyHost(proxyHost).proxyPort(Integer.parseInt(proxyPort));
        }
    }

    public boolean isKmsProxy() {
        return kmsProxy;
    }

    public boolean isS3Proxy() {
        return s3Proxy;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public String getProxyPort() {
        return proxyPort;
    }

    @Override
    public String getDisplayName() {
        return this.bucketName + ":" + this.bucketPath;
    }

    @NonNull
    @Override
    public Secret getPassword() {
        byte[] encryptedString = this.readS3BucketContents();
        String rawString = this.decryptString(encryptedString);
        return Secret.fromString(rawString);
    }

    private byte[] readS3BucketContents() {
        LOGGER.fine("reading s3 bucket");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        S3Object s3Object = this.amazonS3ClientBuilder.build().getObject(new GetObjectRequest(this.bucketName, this.bucketPath));
        try {
            LOGGER.fine("getting s3 bucket contents");
            S3ObjectInputStream objectContent = s3Object.getObjectContent();
            byte[] buffer = new byte[1024]; // 1k buffer
            int read=0;
            while ((read= objectContent.read(buffer,0,buffer.length))!=-1) {
                baos.write(buffer,0,read);
            }
            baos.flush();
        } catch (IOException e) {
            LOGGER.severe("IOException "+e.getMessage());
            throw new AwsBucketReadingException(e);
        } finally {
            try {
                s3Object.close();
            } catch (IOException e) {
                LOGGER.severe("IO Exception closing bucket");
            }
        }
        LOGGER.fine("read contents");
        return baos.toByteArray();
    }

    private String decryptString(byte[] encryptedString) {
        ByteBuffer decryptByteBuffer=null;
        if (null != this.kmsSecretName){
            DecryptRequest request = new DecryptRequest();
            LOGGER.fine("decrypting with kms");
            if (null != this.kmsEncryptionContextKey) {
                LOGGER.info("decrypting with context");
                request.addEncryptionContextEntry(this.kmsEncryptionContextKey, this.kmsSecretName);
            }
            request.setCiphertextBlob(ByteBuffer.wrap(encryptedString));
            DecryptResult decryptResult = this.amazonKmsClientBuilder.build().decrypt(request);
            decryptByteBuffer = decryptResult.getPlaintext();
            LOGGER.fine("decrypted with kms");
        } else {
            LOGGER.fine("no kms secret specified. Assume SSE");
            decryptByteBuffer =ByteBuffer.wrap(encryptedString);
        }
        return Charset.forName("UTF-8").decode(decryptByteBuffer).toString();
    }

    @NonNull
    @Override
    public String getUsername() {
        return this.username;
    }

    public String getBucketName(){
        return this.bucketName;
    }

    public String getBucketPath(){
        return this.bucketPath;
    }

    public String getKmsEncryptionContextKey(){
        return this.kmsEncryptionContextKey;
    }

    public String getKmsSecretName(){
        return this.kmsSecretName;
    }

    public String getRegion() {
        return region;
    }

    @Extension
    public static class DescriptorImpl extends CredentialsDescriptor {

        @Override
        public String getDisplayName() {
            return Messages.AwsBucketCredentialsImpl_DisplayName();
        }
    }
}
