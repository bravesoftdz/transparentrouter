package com.integralblue.transparentrouter.properties;

import java.time.Duration;

import javax.validation.constraints.NotNull;

import com.integralblue.transparentrouter.CleanupOldPendingRepliesConfig;
import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/** Configuration for {@link CleanupOldPendingRepliesConfig}.
 * @author Craig Andrews
 *
 */
@Configuration
@ConfigurationProperties(prefix = "pending-replies.cleanup")
@Validated
@Getter
@Setter
public class CleanupOldPendingRepliesProperties {
	/**
	 * Delete all pending replies created longer ago than this {@link Duration}.
	 */
	@NotNull
	private Duration deletePendingRepliesOlderThan;
}
