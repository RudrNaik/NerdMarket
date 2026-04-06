package NerdMarket.notifications;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnOpen;
import jakarta.websocket.OnMessage;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import jakarta.websocket.server.PathParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

@Controller
@ServerEndpoint(value = "/notifications/{username}")
public class NotificationSocket {

    private static NotificationService notificationService;

    private static UserNotificationRepository userNotificationRepository;

    @Autowired
    public void setNotificationService(NotificationService service) {
        notificationService = service;
    }

    @Autowired
    public void setUserNotificationRepository(UserNotificationRepository repo) {
        userNotificationRepository = repo;
    }

    private static Map<Session, String> sessionUsernameMap = new Hashtable<>();
    private static Map<String, Session> usernameSessionMap = new Hashtable<>();

    private final Logger logger = LoggerFactory.getLogger(NotificationSocket.class);

    @OnOpen
    public void onOpen(Session session, @PathParam("username") String username) throws IOException{
        logger.info("[Notification onOpen] " + username);

        if (usernameSessionMap.containsKey(username)) {
            session.getBasicRemote().sendText("Username already connected");
            session.close();
            return;
        }

        sessionUsernameMap.put(session, username);
        usernameSessionMap.put(username, session);

        //Send unread notification count on connect
        sendMessageToUser(username, "Connected to notifications. Welcome, " + username);
    }

    @OnMessage
    public void onMessage(Session session, String message) throws IOException {
        String username = sessionUsernameMap.get(session);
        logger.info("[Notification onMessage] " + username + ": " + message);

        //Client can send "read:5" to mark notification 5 as read
        if (message.startsWith("read:")) {
            try {
                Long notificationId = Long.valueOf(message.substring(5).trim());
                notificationService.markAsRead(notificationId);
                sendMessageToUser(username, "Notification " + notificationId + "marked as read");
            } catch (Exception e) {
                sendMessageToUser(username, "Error marking notification as read: " + e.getMessage());
            }
        }
    }

    @OnClose
    public void onClose(Session session) throws IOException {
        String username = sessionUsernameMap.get(session);
        logger.info("[Notification onClose] " + username);

        sessionUsernameMap.remove(session);
        usernameSessionMap.remove(username);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        String username = sessionUsernameMap.get(session);
        logger.info("[Notification onError] " + username + ": " + throwable.getMessage());
    }

    //Send a notification message to a specific user that is connected
    public static void sendMessageToUser(String username, String message) {
        try {
            Session session = usernameSessionMap.get(username);
            if (session != null && session.isOpen()) {
                session.getBasicRemote().sendText(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Broadcast a notification to all users that are connected
    public static void broadcast(String message) {
        sessionUsernameMap.forEach((session, username) -> {
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
