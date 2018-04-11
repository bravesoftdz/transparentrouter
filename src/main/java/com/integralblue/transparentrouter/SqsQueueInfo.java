package com.integralblue.transparentrouter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.NonNull;
import lombok.Value;

/** Holds all information about an SQS Queue.
 * @author Craig Andrews
 * @see <a href="https://docs.aws.amazon.com/general/latest/gr/aws-arns-and-namespaces.html">Amazon Resource Names (ARNs) and AWS Service Namespaces</a>
 */
@Value
public class SqsQueueInfo {
	private final static Pattern ARN_PATTERN = Pattern.compile("^arn:(?<partition>.+?):sqs:(?<region>.+?):(?<accountId>.+?):(?<queuename>.+)$");

	private final String partition;
	private final String region;
	private final String accountId;
	private final String queueName;

	public String toArn() {
		return "arn:" + partition + ":sqs:" + region + ":" + accountId + ":" + queueName;
	}

	public static SqsQueueInfo fromArn(final @NonNull String arn) {
		final Matcher matcher = ARN_PATTERN.matcher(arn);
		if (matcher.matches()) {
			final String partition = matcher.group("partition");
			final String region = matcher.group("region");
			final String accountId = matcher.group("accountId");
			final String queueName = matcher.group("queuename");
			return new SqsQueueInfo(partition, region, accountId, queueName);
		}
		else {
			throw new IllegalArgumentException("Provided value is not an SQS Arn: " + arn);
		}
	}
}
