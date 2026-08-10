package com.sujal.itsm.core.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

/**
 * Enterprise WebSocket Configuration Configures STOMP over WebSocket messaging for real-time
 * notifications.
 *
 * <p>Security: Restricts origins to prevent Cross-Site WebSocket Hijacking (CSWSH). Scalability:
 * Configures heartbeats and user-specific destinations. Resilience: Sets message size limits to
 * prevent DoS attacks.
 *
 * @author Enterprise Architecture Team
 * @version 2.0.0
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  // ============================================
  // CONFIGURATION PROPERTIES
  // ============================================

  @Value("${websocket.allowed-origins:http://localhost:9090}")
  private String allowedOrigins;

  @Value("${websocket.heartbeat-interval:25000}") // 25 seconds
  private long heartbeatInterval;

  @Value("${websocket.message-size-limit:131072}") // 128 KB default
  private int messageSizeLimit;

  // ============================================
  // MESSAGE BROKER CONFIGURATION
  // ============================================

  @Override
  public void configureMessageBroker(@NonNull MessageBrokerRegistry config) {
    // Enable a simple in-memory message broker.
    // /topic - For broadcasting messages to all subscribers (e.g., global alerts).
    // /queue - For user-specific messages (e.g., personal notifications).
    config
        .enableSimpleBroker("/topic", "/queue")
        .setHeartbeatValue(new long[] {heartbeatInterval, heartbeatInterval})
        .setTaskScheduler(webSocketHeartbeatTaskScheduler());

    // Prefix for messages FROM the client TO the server (e.g., /app/chat).
    config.setApplicationDestinationPrefixes("/app");

    // Prefix for user-specific messaging (e.g., /user/queue/notifications).
    // Requires a Principal to be set in the WebSocket session (handled by Spring Security).
    config.setUserDestinationPrefix("/user");
  }

  // ============================================
  // ENDPOINT REGISTRATION (SECURITY)
  // ============================================

  @Override
  public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
    // The endpoint where the WebSocket connection is established.
    registry
        .addEndpoint("/ws")
        // Security: Restrict origins to prevent Cross-Site WebSocket Hijacking
        .setAllowedOriginPatterns(allowedOrigins.split(","))
        // Fallback for older browsers or corporate firewalls that block raw WebSockets
        .withSockJS();
  }

  // ============================================
  // TRANSPORT CONFIGURATION (RESILIENCE)
  // ============================================

  @Override
  public void configureWebSocketTransport(@NonNull WebSocketTransportRegistration registration) {
    // Security & Resilience: Limit message size to prevent DoS via large payloads
    registration.setMessageSizeLimit(messageSizeLimit);

    // Resilience: Configure send buffer size and send time limit
    registration.setSendBufferSizeLimit(512 * 1024); // 512 KB
    registration.setSendTimeLimit(20_000); // 20 seconds
  }

  // ============================================
  // CHANNEL CONFIGURATION (PERFORMANCE)
  // ============================================

  @Override
  public void configureClientInboundChannel(@NonNull ChannelRegistration registration) {
    // Performance: Configure bounded thread pool for handling incoming messages
    registration.taskExecutor().corePoolSize(4).maxPoolSize(8).queueCapacity(100);
  }

  @Override
  public void configureClientOutboundChannel(@NonNull ChannelRegistration registration) {
    // Performance: Configure bounded thread pool for handling outgoing messages
    registration.taskExecutor().corePoolSize(4).maxPoolSize(8).queueCapacity(100);
  }

  // ============================================
  // BEANS
  // ============================================

  /**
   * Provides a TaskScheduler for handling WebSocket heartbeats. This is critical for detecting and
   * cleaning up dead connections.
   */
  @Bean
  public ThreadPoolTaskScheduler webSocketHeartbeatTaskScheduler() {
    ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
    taskScheduler.setPoolSize(1);
    taskScheduler.setThreadNamePrefix("ws-heartbeat-");
    return taskScheduler;
  }
}
