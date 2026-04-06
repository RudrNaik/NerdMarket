package NerdMarket.debugging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@Component
public class ScanningWebSocketEventListener {

    private static final Logger log = LoggerFactory.getLogger(ScanningWebSocketEventListener.class);

    @EventListener
    public void handleConnect(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        log.info("[WS] Client connecting - session: {}", accessor.getSessionId());
    }

    @EventListener
    public void handleConnected(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        log.info("[WS] Client connected - session: {}", accessor.getSessionId());
    }

    @EventListener
    public void handleSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        log.info("[WS] Client subscribed to {} - session: {}", accessor.getDestination(), accessor.getSessionId());
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        CloseStatus closeStatus = event.getCloseStatus();
        int code = closeStatus != null ? closeStatus.getCode() : -1;

        if (code != CloseStatus.NORMAL.getCode()) {
            log.warn("[WS] Abnormal disconnect - session: {}, close status: {}", sessionId, closeStatus);
        } else {
            log.info("[WS] Client disconnected - session: {}, close status: {}", sessionId, closeStatus);
        }
    }
}
