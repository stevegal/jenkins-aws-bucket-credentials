package uk.co.stevegal.jenkins.plugins.awsbucketcredentials;


import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.domains.Domain;
import hudson.Functions;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Item;
import hudson.model.Run;
import hudson.tasks.BatchFile;
import hudson.tasks.Shell;
import hudson.util.Secret;
import org.jenkinsci.plugins.credentialsbinding.MultiBinding;
import org.jenkinsci.plugins.credentialsbinding.impl.SecretBuildWrapper;
import org.jenkinsci.plugins.credentialsbinding.impl.UsernamePasswordMultiBinding;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Created by stevegal on 05/02/2017.
 */
public class AwsBucketCredentialsBindingTest {

    private AwsBucketCredentialsBinding test = new AwsBucketCredentialsBinding("userName","password","id");

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    @Test
    public void typeIsTheSameAsBucketCredentials(){

        Class clazz = test.type();
        assertThat(clazz).isSameAs(AwsBucketCredentials.class);
    }

    @Test
    public void listsAllVariables() {
        Set<String> actualVariables = test.variables();
        assertThat(actualVariables).hasSize(2).containsExactlyInAnyOrder("userName","password");
    }

    @Test
    public void defaultVariables() {
        AwsBucketCredentialsBinding test = new AwsBucketCredentialsBinding(null,null,"id");
        Set<String> actualVariables = test.variables();
        assertThat(actualVariables).hasSize(2).containsExactlyInAnyOrder("BUCKET_USER_NAME","BUCKET_PASSWORD");
    }

    /**
     * this is an integration test as the bind is a bit tricky to mock out
     * @throws Exception
     */
    @Test
    public void bindAddsNoNewVariablesAndValuesYet() throws Exception{
        AwsBucketCredentialsImpl credentials = mock(AwsBucketCredentialsImpl.class);
        when(credentials.getId()).thenReturn("id");
        when(credentials.getPassword()).thenReturn(Secret.fromString("password"));
        when(credentials.getUsername()).thenReturn("username");
        CredentialsProvider.lookupStores(jenkinsRule.jenkins).iterator().next().addCredentials(Domain.global(), credentials);

        FreeStyleProject p = jenkinsRule.createFreeStyleProject();
        p.getBuildWrappersList().add(new SecretBuildWrapper(Collections.<MultiBinding<?>>singletonList(new AwsBucketCredentialsBinding("userid", "pass", "id"))));
        if (Functions.isWindows()) {
            p.getBuildersList().add(new BatchFile("@echo off\necho %userid%/%pass% > auth.txt"));
        } else {
            p.getBuildersList().add(new Shell("set +x\necho $userid/$pass > auth.txt"));
        }
        jenkinsRule.configRoundtrip((Item)p);
        SecretBuildWrapper wrapper = p.getBuildWrappersList().get(SecretBuildWrapper.class);
        assertThat(wrapper).isNotNull();
        List<? extends MultiBinding<?>> bindings = wrapper.getBindings();
        assertThat(bindings).hasSize(1);
        MultiBinding<?> binding = bindings.get(0);

        assertThat(((AwsBucketCredentialsBinding) binding).getUsernameVariable()).isEqualTo("userid");
        assertThat(((AwsBucketCredentialsBinding) binding).getPasswordVariable()).isEqualTo("pass");
        FreeStyleBuild b = jenkinsRule.buildAndAssertSuccess(p);
        assertThat(b.getWorkspace().child("auth.txt").readToString().trim()).contains("username/password");

    }

    @Test
    public void descriptorHasCorrectCredentials() {
        AwsBucketCredentialsBinding.DescriptorImpl descriptorTest = new AwsBucketCredentialsBinding.DescriptorImpl();
        Class type = descriptorTest.type();
        assertThat(type).isSameAs(AwsBucketCredentials.class);
    }

    @Test
    public void descriptorHasCorrectDisplayName() {
        AwsBucketCredentialsBinding.DescriptorImpl descriptorTest = new AwsBucketCredentialsBinding.DescriptorImpl();
        String displayName = descriptorTest.getDisplayName();
        assertThat(displayName).isEqualTo("Aws Bucket credentials");

    }


}