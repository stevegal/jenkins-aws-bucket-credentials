package uk.co.stevegal.jenkins.plugins.awsbucketcredentials;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.kms.AWSKMSClient;
import com.amazonaws.services.s3.AmazonS3Client;
import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

/**
 * Created by stevegal on 07/02/2017.
 */
public class AwsKmsClientBuilderTest {
    @Test
    public void regionAndProxyShouldBeReflectedInClient(){
        AwsKmsClientBuilder clientBuilder = new AwsKmsClientBuilder();
        clientBuilder.region("eu-west-1").proxyHost("host").proxyPort(8080);
        AWSKMSClient amazonKmsClient = clientBuilder.build();
        {
            ClientConfiguration configuration = (ClientConfiguration) Whitebox.getInternalState(amazonKmsClient,"clientConfiguration");
            assertThat(configuration.getProxyHost()).isEqualTo("host");
            assertThat(configuration.getProxyPort()).isEqualTo(8080);
        }
    }

    @Test
    public void regionSetButHostEmpty(){
        AwsKmsClientBuilder clientBuilder = new AwsKmsClientBuilder();
        clientBuilder.region("eu-west-1").proxyPort(8080);
        AWSKMSClient amazonKmsClient = clientBuilder.build();
        {
            ClientConfiguration configuration = (ClientConfiguration)Whitebox.getInternalState(amazonKmsClient,"clientConfiguration");
            assertThat(configuration.getProxyHost()).isNull();
            assertThat(configuration.getProxyPort()).isEqualTo(-1);
        }
    }

    @Test
    public void regionSetButHostFilledWithSpaces() {
        AwsKmsClientBuilder clientBuilder = new AwsKmsClientBuilder();
        clientBuilder.region("eu-west-1").proxyHost("    ").proxyPort(8080);
        AWSKMSClient amazonS3Client = clientBuilder.build();
        {
            ClientConfiguration configuration = (ClientConfiguration)Whitebox.getInternalState(amazonS3Client,"clientConfiguration");
            assertThat(configuration.getProxyHost()).isNull();
            assertThat(configuration.getProxyPort()).isEqualTo(-1);
        }
    }

    @Test
    public void regionNotSet(){
        AwsKmsClientBuilder clientBuilder = new AwsKmsClientBuilder();
        AWSKMSClient amazonKmsClient = clientBuilder.build();

        URI endpoint =(URI)Whitebox.getInternalState(amazonKmsClient,"endpoint");
        assertThat(endpoint.toString()).contains("us-east-1");
    }

    @Test
    public void regionSet(){
        AwsKmsClientBuilder clientBuilder = new AwsKmsClientBuilder();
        AWSKMSClient amazonKmsClient = clientBuilder.region("eu-west-1").build();

        URI endpoint =(URI)Whitebox.getInternalState(amazonKmsClient,"endpoint");
        assertThat(endpoint.toString()).contains("eu-west-1");
    }

    @Test
    public void regionWasEmptyString(){
        AwsKmsClientBuilder clientBuilder = new AwsKmsClientBuilder();
        AWSKMSClient amazonKmsClient = clientBuilder.region("   ").build();

        URI endpoint =(URI)Whitebox.getInternalState(amazonKmsClient,"endpoint");
        assertThat(endpoint.toString()).contains("us-east-1");
    }
}