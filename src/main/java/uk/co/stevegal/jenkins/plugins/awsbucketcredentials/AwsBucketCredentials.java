package uk.co.stevegal.jenkins.plugins.awsbucketcredentials;

import com.cloudbees.plugins.credentials.CredentialsNameProvider;
import com.cloudbees.plugins.credentials.NameWith;
import com.cloudbees.plugins.credentials.common.StandardUsernameCredentials;
import com.cloudbees.plugins.credentials.common.UsernamePasswordCredentials;
import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Util;

/**
 * Created by stevegal on 05/02/2017.
 * A credentials provider that will reach into an S3 bucket to retrieve a password file
 */
@NameWith(value=AwsBucketCredentials.NameProvider.class, priority = 1)
public interface AwsBucketCredentials extends StandardUsernameCredentials, UsernamePasswordCredentials {

    String getDisplayName();

    class NameProvider extends CredentialsNameProvider<AwsBucketCredentials> {

        @NonNull
        @Override
        public String getName(@NonNull AwsBucketCredentials awsBucketCredentials) {
            String description = Util.fixEmpty(awsBucketCredentials.getDescription());
            return awsBucketCredentials.getDisplayName()+ (description==null?"":" ("+description+")");
        }
    }
}
