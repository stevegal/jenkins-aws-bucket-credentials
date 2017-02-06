package uk.co.stevegal.jenkins.plugins.awsbucketcredentials;

import com.amazonaws.auth.policy.resources.S3BucketResource;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.kms.AWSKMSClient;
import com.amazonaws.services.kms.model.DecryptRequest;
import com.amazonaws.services.kms.model.DecryptResult;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.cloudbees.plugins.credentials.CredentialsDescriptor;
import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;
import com.google.inject.Inject;
import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.util.Secret;
import org.apache.commons.lang.NotImplementedException;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Created by stevegal on 05/02/2017.
 */
public class AwsBucketCredentialsImpl extends BaseStandardCredentials implements AwsBucketCredentials {

    private final String bucketName;
    private final String bucketPath;
    private String kmsEncryptionContextKey;
    private final String kmsSecretName;
    private final Charset charset = Charset.forName("UTF-8");
    private String username;
    private AmazonS3Client amazonS3Client;
    private AWSKMSClient amazonKmsClient;


    private static final Logger LOGGER = Logger.getLogger(AwsBucketCredentialsImpl.class.getName());

    @DataBoundConstructor
    public AwsBucketCredentialsImpl(@CheckForNull CredentialsScope scope,@CheckForNull String id,
                                    @CheckForNull String bucketName, @CheckForNull String bucketPath,
                                    @CheckForNull String username, @CheckForNull String description,
                                    @CheckForNull String kmsEncryptionContextKey, @CheckForNull String kmsSecretName) {
        super(scope, id, description);
        this.bucketName = bucketName;
        this.bucketPath = bucketPath;
        this.kmsEncryptionContextKey = kmsEncryptionContextKey;
        this.kmsSecretName = kmsSecretName;
        this.username = username;
        this.amazonS3Client= new AmazonS3Client(new ProfileCredentialsProvider());
        this.amazonKmsClient= new AWSKMSClient();
    }

    @Override
    public String getDisplayName() {
        return this.bucketName+":"+this.bucketPath;
    }

    @NonNull
    @Override
    public Secret getPassword() {
        String encryptedString = this.readS3BucketContents();
        String rawString = this.decryptString(encryptedString);
        return Secret.fromString(rawString);
    }

    private String readS3BucketContents() {
        LOGGER.fine("reading s3 bucket");
        S3Object s3Object = this.amazonS3Client.getObject(new GetObjectRequest(this.bucketName, this.bucketPath));
        LOGGER.fine("getting s3 bucket contents");
        S3ObjectInputStream objectContent = s3Object.getObjectContent();
        BufferedReader reader = new BufferedReader(new InputStreamReader(objectContent));
        StringBuilder builder = new StringBuilder();
        String line;
        try {
            while((line = reader.readLine()) != null) {
                builder.append(line);
            }
        } catch (IOException e) {
            LOGGER.severe("IO Exception reading content");
            throw new NotImplementedException();
        }
        LOGGER.fine("read contents");
        return builder.toString();
    }

    private String decryptString(String encryptedString) {
        DecryptRequest request = new DecryptRequest();
        LOGGER.fine("decrypting with kms");
        if (null!=this.kmsEncryptionContextKey && null!=this.kmsSecretName) {
            LOGGER.info("decrypting with context");
            request.addEncryptionContextEntry(this.kmsEncryptionContextKey, this.kmsSecretName);
        }
        request.setCiphertextBlob(charset.encode(encryptedString));
        DecryptResult decryptResult = this.amazonKmsClient.decrypt(request);
        LOGGER.fine("decrypted with kms");
        return charset.decode(decryptResult.getPlaintext()).toString();
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

    @Extension
    public static class DescriptorImpl extends CredentialsDescriptor {

        @Override
        public String getDisplayName() {
            return Messages.AwsBucketCredentialsImpl_DisplayName();
        }
    }
}
