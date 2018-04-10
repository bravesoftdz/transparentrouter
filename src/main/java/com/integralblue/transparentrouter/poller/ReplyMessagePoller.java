package com.integralblue.transparentrouter.poller;

import com.integralblue.transparentrouter.QueueProperties.RouteConfiguration;
import com.integralblue.transparentrouter.sqsmessagelistener.ReplySqsMessageListener;
import lombok.NonNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/** Reads messages from the reply queues and handles them with {@link ReplySqsMessageListener}.
 * @author Craig Andrews
 *
 */
@Component
public class ReplyMessagePoller extends AbstractMessagePoller {

	@Autowired
	public ReplyMessagePoller(final @NonNull ReplySqsMessageListener sqsMessageListener) {
		super(sqsMessageListener);
	}

	@Override
	protected String getListeningQueueArnFromRouteConfiguration(final @NonNull RouteConfiguration routeConfiguration) {
		return routeConfiguration.getReplyArn();
	}

}
