package uk.co.stevegal.jenkins.plugins.awsbucketcredentials;

import edu.umd.cs.findbugs.annotations.Nullable;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.credentialsbinding.BindingDescriptor;
import org.jenkinsci.plugins.credentialsbinding.MultiBinding;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.*;

/**
 * Created by stevegal on 05/02/2017.
 * This binds the AwsBucketCredentials to multiple jenkins environments
 */
public class AwsBucketCredentialsBinding extends MultiBinding<AwsBucketCredentials> {

    public static final String DEFAULT_USERNAME_VARIABLE = "BUCKET_USER_NAME";
    public static final String DEFAULT_PASSWORD_VARIABLE = "BUCKET_PASSWORD";
    private String usernameVariable;
    private String passwordVariable;

    /**
     * For use with {@link DataBoundConstructor}.
     *
     * @param credentialsId
     */
    @DataBoundConstructor
    public AwsBucketCredentialsBinding(@Nullable String usernameVariable, @Nullable String passwordVariable,
                                       String credentialsId) {
        super(credentialsId);
        this.usernameVariable = StringUtils.defaultIfBlank(usernameVariable, DEFAULT_USERNAME_VARIABLE);
        this.passwordVariable = StringUtils.defaultIfBlank(passwordVariable, DEFAULT_PASSWORD_VARIABLE);
    }

    @Override
    protected Class<AwsBucketCredentials> type() {
        return AwsBucketCredentials.class;
    }

    @Override
    public MultiEnvironment bind(@Nonnull Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener) throws IOException, InterruptedException {
        AwsBucketCredentials credentials = this.getCredentials(build);
        Map<String, String> map = new HashMap<String, String>();
        map.put(this.usernameVariable,credentials.getUsername());
        map.put(this.passwordVariable,credentials.getPassword().getPlainText());
        return new MultiEnvironment(map);
    }

    public String getUsernameVariable() {
        return usernameVariable;
    }

    public String getPasswordVariable() {
        return passwordVariable;
    }

    @Override
    public Set<String> variables() {
        Set<String> variables = new HashSet<String>();
        variables.add(this.usernameVariable);
        variables.add(this.passwordVariable);
        return variables;
    }

    @Extension
    public static class DescriptorImpl extends BindingDescriptor<AwsBucketCredentials> {

        public DescriptorImpl() {
            System.out.print("hello binding");
        }

        @Override
        protected Class<AwsBucketCredentials> type() {
            return AwsBucketCredentials.class;
        }

        @Override
        public String getDisplayName() {
            return "Aws Bucket credentials";
        }
    }
}
