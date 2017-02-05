package uk.co.stevegal.jenkins.plugins.awsbucketcredentials;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Created by stevegal on 05/02/2017.
 */
public class AwsBucketCredentialsTest {

    @Test
    public void nameProviderReturnsNameWithConfig(){

        String description = this.doTest("role:myRole,bucket:arn1");

        assertThat(description).isEqualTo("AwsBucketCredentials (role:myRole,bucket:arn1)");
    }

    @Test
    public void nameProviderHandlesNullDescription(){
        String description = this.doTest(null);

        assertThat(description).isEqualTo("AwsBucketCredentials");
    }

    @Test
    public void nameProviderHandlesEmptyDescription(){

        String description = this.doTest("");

        assertThat(description).isEqualTo("AwsBucketCredentials");
    }

    private String doTest(String description) {
        AwsBucketCredentials.NameProvider provider = new AwsBucketCredentials.NameProvider();

        AwsBucketCredentials mockCredentials = mock(AwsBucketCredentials.class);
        when(mockCredentials.getDescription()).thenReturn(description);
        when(mockCredentials.getDisplayName()).thenReturn("AwsBucketCredentials");

        return provider.getName(mockCredentials);
    }

}