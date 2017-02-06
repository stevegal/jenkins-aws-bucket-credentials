package uk.co.stevegal.jenkins.plugins.awsbucketcredentials;

/**
 * Created by stevegal on 06/02/2017.
 */
public class AwsBucketReadingException extends RuntimeException {
    public  AwsBucketReadingException(Exception cause) {
        super(cause);
    }
}
