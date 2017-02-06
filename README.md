This is a jenkins plugin to retrieve encrypted credentials from an Amazon S3 bucket that is encrypted with KMS

The variables are:
bucketName the name of the bucket in
bucketPath the object id within the bucket where the encrypted value where the cipher text should be read from

KMS is used to decrypt the cipher text
You can optionally provide a context key and value within the script if you used this to encrypt with.
