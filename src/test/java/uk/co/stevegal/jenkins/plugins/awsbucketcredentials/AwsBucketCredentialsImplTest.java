package uk.co.stevegal.jenkins.plugins.awsbucketcredentials;

import com.amazonaws.services.kms.AWSKMSClient;
import com.amazonaws.services.kms.model.DecryptRequest;
import com.amazonaws.services.kms.model.DecryptResult;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.cloudbees.plugins.credentials.CredentialsScope;
import hudson.util.Secret;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.HudsonTestCase;
import org.jvnet.hudson.test.JenkinsRule;
import org.mockito.ArgumentCaptor;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyByte;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Created by stevegal on 05/02/2017.
 */
public class AwsBucketCredentialsImplTest {

    private AwsBucketCredentialsImpl test = new AwsBucketCredentialsImpl(CredentialsScope.GLOBAL, "myId",
            "bucketUri", "/bucketPath", "username",
            "mydescription","someEncryptContextKey", "kmsEncryptContextValue");

    private AmazonS3Client mockClient;
    private AWSKMSClient mockKmsClient;

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    @Before
    public void setupMocks() {
        this.mockClient = mock(AmazonS3Client.class);
        this.mockKmsClient = mock(AWSKMSClient.class);
        Whitebox.setInternalState(test, "amazonS3Client", mockClient);
        Whitebox.setInternalState(test,"amazonKmsClient", mockKmsClient);

    }

    @Test
    public void calculatesMeaningfulDisplayName() {
        String displayName = test.getDisplayName();
        assertThat(displayName).isEqualTo("bucketUri:/bucketPath");
    }

    @Test
    public void descriptorHasADisplayName() {
        AwsBucketCredentialsImpl.DescriptorImpl descriptorTest = new AwsBucketCredentialsImpl.DescriptorImpl();
        String displayName = descriptorTest.getDisplayName();
        assertThat(displayName).isEqualTo("AWS Bucket Credentials");
    }

    @Test
    public void username() {
        String username = test.getUsername();
        assertThat(username).isEqualTo("username");
    }

    @Test
    public void passwordUsesTheS3Bucket() throws Exception {
        S3Object mockS3Object = mock(S3Object.class);
        when(mockClient.getObject(any(GetObjectRequest.class))).thenReturn(mockS3Object);
        S3ObjectInputStream mockS3ObjectInputStream = mock(S3ObjectInputStream.class);
        when(mockS3Object.getObjectContent()).thenReturn(mockS3ObjectInputStream);
        when(mockS3ObjectInputStream.read(new byte[anyInt()], anyInt(), anyByte()))
                .thenAnswer(new WriteBufferAnswer("encryptedPassword".getBytes()))
                .thenReturn(-1);

        DecryptResult result = new DecryptResult();
        CharsetEncoder charsetEncoder = Charset.forName("UTF-8").newEncoder();
        result.setPlaintext(charsetEncoder.encode(CharBuffer.wrap("password")));
        when(mockKmsClient.decrypt(any(DecryptRequest.class))).thenReturn(result);

        Secret secret = test.getPassword();

        // have we got the expected password
        assertThat(secret.getPlainText()).isEqualTo("password");

        // have we used the bucket
        ArgumentCaptor<GetObjectRequest> capturedObjectRequest = ArgumentCaptor.forClass(GetObjectRequest.class);
        verify(mockClient).getObject(capturedObjectRequest.capture());
        assertThat(capturedObjectRequest.getValue().getBucketName()).isEqualTo("bucketUri");
        assertThat(capturedObjectRequest.getValue().getS3ObjectId().getKey()).isEqualTo("/bucketPath");

        // have we used kms to decrypt
        ArgumentCaptor<DecryptRequest> capturedDecryptRequest = ArgumentCaptor.forClass(DecryptRequest.class);
        verify(mockKmsClient).decrypt(capturedDecryptRequest.capture());
        assertThat(capturedDecryptRequest.getValue().getEncryptionContext()).containsEntry("someEncryptContextKey","kmsEncryptContextValue");
        ByteBuffer ciphertextBlob = capturedDecryptRequest.getValue().getCiphertextBlob();
        assertThat(new String(Charset.forName("UTF-8").decode(ciphertextBlob).array())).isEqualTo("encryptedPassword");

    }

    @Test
    public void doesNotUseEncryptContextIfNotProvided() throws Exception {

        AwsBucketCredentialsImpl test = new AwsBucketCredentialsImpl(CredentialsScope.GLOBAL, "myId",
                "bucketUri", "/bucketPath", "username",
                "mydescription",null, null);
        Whitebox.setInternalState(test, "amazonS3Client", mockClient);
        Whitebox.setInternalState(test,"amazonKmsClient", mockKmsClient);

        S3Object mockS3Object = mock(S3Object.class);
        when(mockClient.getObject(any(GetObjectRequest.class))).thenReturn(mockS3Object);
        S3ObjectInputStream mockS3ObjectInputStream = mock(S3ObjectInputStream.class);
        when(mockS3Object.getObjectContent()).thenReturn(mockS3ObjectInputStream);
        when(mockS3ObjectInputStream.read(new byte[anyInt()], anyInt(), anyByte()))
                .thenAnswer(new WriteBufferAnswer("encryptedPassword".getBytes()))
                .thenReturn(-1);

        DecryptResult result = new DecryptResult();
        CharsetEncoder charsetEncoder = Charset.forName("UTF-8").newEncoder();
        result.setPlaintext(charsetEncoder.encode(CharBuffer.wrap("password")));
        when(mockKmsClient.decrypt(any(DecryptRequest.class))).thenReturn(result);

        Secret secret = test.getPassword();

        // have we got the expected password
        assertThat(secret.getPlainText()).isEqualTo("password");

        // have we used the bucket
        ArgumentCaptor<GetObjectRequest> capturedObjectRequest = ArgumentCaptor.forClass(GetObjectRequest.class);
        verify(mockClient).getObject(capturedObjectRequest.capture());
        assertThat(capturedObjectRequest.getValue().getBucketName()).isEqualTo("bucketUri");
        assertThat(capturedObjectRequest.getValue().getS3ObjectId().getKey()).isEqualTo("/bucketPath");

        // have we used kms to decrypt
        ArgumentCaptor<DecryptRequest> capturedDecryptRequest = ArgumentCaptor.forClass(DecryptRequest.class);
        verify(mockKmsClient).decrypt(capturedDecryptRequest.capture());
        assertThat(capturedDecryptRequest.getValue().getEncryptionContext()).isEmpty();
        ByteBuffer ciphertextBlob = capturedDecryptRequest.getValue().getCiphertextBlob();
        assertThat(new String(Charset.forName("UTF-8").decode(ciphertextBlob).array())).isEqualTo("encryptedPassword");

    }

    private static class WriteBufferAnswer implements Answer<Integer> {

        private byte[] bytes;

        public WriteBufferAnswer(byte[] bytes) {
            this.bytes = bytes;
        }

        @Override
        public Integer answer(InvocationOnMock invocation) throws Throwable {
            byte[] buffer = (byte[]) invocation.getArguments()[0];
            System.arraycopy(bytes, 0, buffer, 0, bytes.length);
            return bytes.length;
        }
    }

}