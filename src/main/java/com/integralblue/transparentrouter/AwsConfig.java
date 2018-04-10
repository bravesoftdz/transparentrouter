package com.integralblue.transparentrouter;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.AwsRegionProvider;
import com.amazonaws.regions.AwsRegionProviderChain;
import com.amazonaws.regions.DefaultAwsRegionProviderChain;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.util.StringUtils;
import com.integralblue.transparentrouter.aws.ExtendedAmazonSQS;
import com.integralblue.transparentrouter.aws.ExtendedAmazonSQSImpl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Set up AWS components.
 * @author Craig Andrews
 *
 */
@Configuration
public class AwsConfig {
	@Value("${aws.accessKeyId:#{null}}")
	private String awsAccessKeyId;

	@Value("${aws.secretKey:#{null}}")
	private String awsSecretKey;

	@Value("${aws.region:#{null}}")
	private String awsRegion;

	@Bean
	public AWSCredentialsProvider awsCredentialsProvider() {
		return new AWSCredentialsProviderChain(
				new SpringPropertiesAWSCredentialsProvider(),
				new DefaultAWSCredentialsProviderChain());
	}

	@Bean
	public AwsRegionProvider awsRegionProvider() {
		return new AwsRegionProviderChain(
				new SpringPropertiesAwsRegionProvider(),
				new DefaultAwsRegionProviderChain());
	}

	@Bean(destroyMethod = "shutdown")
	public ExtendedAmazonSQS amazonSQS() {
		return new ExtendedAmazonSQSImpl(AmazonSQSClientBuilder.standard()
				.withCredentials(awsCredentialsProvider())
				.withRegion(awsRegionProvider().getRegion())
				.build());
	}

	/** Read the AWS credentials from Spring properties (ex, such as those defined in application.properties).
	 * @author Craig Andrews
	 *
	 */
	private class SpringPropertiesAWSCredentialsProvider implements AWSCredentialsProvider {

		@Override
		public AWSCredentials getCredentials() {
			if (StringUtils.isNullOrEmpty(awsAccessKeyId) || StringUtils.isNullOrEmpty(awsSecretKey)) {
				throw new SdkClientException(
					"Unable to load AWS credentials from Spring "
					+ "properties");
		}
			return new BasicAWSCredentials(awsAccessKeyId, awsSecretKey);
		}

		@Override
		public void refresh() {
			// refreshing these credentials doesn't make sense, so don't do anything
		}

	}

	/** Read the AWS region from Spring properties (ex, such as those defined in application.properties).
	 * @author Craig Andrews
	 *
	 */
	private class SpringPropertiesAwsRegionProvider extends AwsRegionProvider {

		@Override
		public String getRegion() throws SdkClientException {
			return awsRegion;
		}

	}
}
