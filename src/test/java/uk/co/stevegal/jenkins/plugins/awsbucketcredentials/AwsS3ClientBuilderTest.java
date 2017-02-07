package uk.co.stevegal.jenkins.plugins.awsbucketcredentials;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;
import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by stevegal on 07/02/2017.
 */
public class AwsS3ClientBuilderTest {

    @Test
    public void regionAndProxyShouldBeReflectedInClient(){
        AwsS3ClientBuilder clientBuilder = new AwsS3ClientBuilder();
        clientBuilder.region("eu-west-1").proxyHost("host").proxyPort(8080);
        AmazonS3Client amazonS3Client = clientBuilder.build();
        assertThat(amazonS3Client.getRegion().toString()).isEqualTo(Region.getRegion(Regions.EU_WEST_1).toString());
        {
            ClientConfiguration configuration = (ClientConfiguration)Whitebox.getInternalState(amazonS3Client,"clientConfiguration");
            assertThat(configuration.getProxyHost()).isEqualTo("host");
            assertThat(configuration.getProxyPort()).isEqualTo(8080);
        }
    }

    @Test
    public void regionSetButHostEmpty(){
        AwsS3ClientBuilder clientBuilder = new AwsS3ClientBuilder();
        clientBuilder.region("eu-west-1").proxyPort(8080);
        AmazonS3Client amazonS3Client = clientBuilder.build();
        assertThat(amazonS3Client.getRegion().toString()).isEqualTo(Region.getRegion(Regions.EU_WEST_1).toString());
        {
            ClientConfiguration configuration = (ClientConfiguration)Whitebox.getInternalState(amazonS3Client,"clientConfiguration");
            assertThat(configuration.getProxyHost()).isNull();
            assertThat(configuration.getProxyPort()).isEqualTo(-1);
        }
    }

    @Test
    public void regionNotSet(){
        AwsS3ClientBuilder clientBuilder = new AwsS3ClientBuilder();
        AmazonS3Client amazonS3Client = clientBuilder.build();

        assertThat(amazonS3Client.getRegion().toString()).isNull();
    }

    @Test
    public void regionWasEmptyString(){
        AwsS3ClientBuilder clientBuilder = new AwsS3ClientBuilder();
        AmazonS3Client amazonS3Client = clientBuilder.region("   ").build();

        assertThat(amazonS3Client.getRegion().toString()).isNull();
    }

    @Test
    public void regionSetButHostFilledWithSpaces() {
        AwsS3ClientBuilder clientBuilder = new AwsS3ClientBuilder();
        clientBuilder.region("eu-west-1").proxyHost("    ").proxyPort(8080);
        AmazonS3Client amazonS3Client = clientBuilder.build();
        assertThat(amazonS3Client.getRegion().toString()).isEqualTo(Region.getRegion(Regions.EU_WEST_1).toString());
        {
            ClientConfiguration configuration = (ClientConfiguration)Whitebox.getInternalState(amazonS3Client,"clientConfiguration");
            assertThat(configuration.getProxyHost()).isNull();
            assertThat(configuration.getProxyPort()).isEqualTo(-1);
        }
    }
}