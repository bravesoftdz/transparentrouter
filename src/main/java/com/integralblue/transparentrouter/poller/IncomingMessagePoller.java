package com.integralblue.transparentrouter.poller;

import com.integralblue.transparentrouter.QueueProperties.RouteConfiguration;
import com.integralblue.transparentrouter.sqsmessagelistener.IncomingSqsMessageListener;
import lombok.NonNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** Reads messages from the incoming queues and handles them with {@link IncomingSqsMessageListener}.
 * @author Craig Andrews
 *
 */
@Component
public class IncomingMessagePoller extends AbstractMessagePoller {

	@Autowired
	public IncomingMessagePoller(final IncomingSqsMessageListener sqsMessageListener) {
		super(sqsMessageListener);
	}

	@Override
	protected String getListeningQueueArnFromRouteConfiguration(final @NonNull RouteConfiguration routeConfiguration) {
		return routeConfiguration.getIncomingArn();
	}

}
