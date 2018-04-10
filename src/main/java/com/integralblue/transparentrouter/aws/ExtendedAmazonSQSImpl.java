package com.integralblue.transparentrouter.aws;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.GetQueueUrlRequest;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.integralblue.transparentrouter.SqsQueueInfo;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

import org.springframework.cache.annotation.Cacheable;

/** Wraps the given {@link AmazonSQS}, extending it with some additional useful methods.
 * @author Craig Andrews
 *
 */
@RequiredArgsConstructor
public class ExtendedAmazonSQSImpl implements ExtendedAmazonSQS {
	@Delegate
	private final AmazonSQS delegate;

	/* (non-Javadoc)
	 * @see com.integralblue.transparentrouter.aws.ExtendedAmazonSQS#getQueueUrlFromArn(java.lang.String)
	 */
	@Override
	@Cacheable // the return value never changes so mind as well cache it to speed things up a bit
	public GetQueueUrlResult getQueueUrlFromArn(final @NonNull String arn) {
		final SqsQueueInfo sqsQueueInfo = SqsQueueInfo.fromArn(arn);
		return getQueueUrl(new GetQueueUrlRequest().withQueueName(sqsQueueInfo.getQueueName()).withQueueOwnerAWSAccountId(sqsQueueInfo.getAccountId()));
	}

	/* (non-Javadoc)
	 * @see com.integralblue.transparentrouter.aws.ExtendedAmazonSQS#forwardMessage(com.amazonaws.services.sqs.model.Message, java.lang.String)
	 */
	@Override
	public void forwardMessage(final @NonNull Message message, final @NonNull String queueArn) {
		final SendMessageRequest sendMessageRequest = new SendMessageRequest()
				.withMessageAttributes(message.getMessageAttributes())
				.withMessageBody(message.getBody())
				.withQueueUrl(getQueueUrlFromArn(queueArn).getQueueUrl());
		sendMessage(sendMessageRequest);
	}
}
