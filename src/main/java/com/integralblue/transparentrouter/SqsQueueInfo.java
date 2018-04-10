package com.integralblue.transparentrouter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.NonNull;
import lombok.Value;

/** Holds all information about an SQS Queue.
 * @author Craig Andrews
 *
 */
@Value
public class SqsQueueInfo {
	private final static Pattern ARN_PATTERN = Pattern.compile("^arn:aws:sqs:(?<region>.+?):(?<accountId>.+?):(?<queuename>.+)$");

	private final String region;
	private final String accountId;
	private final String queueName;

	public String toArn() {
		return "arn:aws:sqs:" + region + ":" + accountId + ":" + queueName;
	}

	public static SqsQueueInfo fromArn(final @NonNull String arn) {
		final Matcher matcher = ARN_PATTERN.matcher(arn);
		if (matcher.matches()) {
			final String region = matcher.group("region");
			final String accountId = matcher.group("accountId");
			final String queueName = matcher.group("queuename");
			return new SqsQueueInfo(region, accountId, queueName);
		}
		else {
			throw new IllegalArgumentException("Provided value is not an SQS Arn: " + arn);
		}
	}
}
