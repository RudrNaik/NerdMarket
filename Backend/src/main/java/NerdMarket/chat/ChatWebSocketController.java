package NerdMarket.chat;

import NerdMarket.users.UserRepository;
import NerdMarket.users.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class ChatWebSocketController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat/{roomId}")
    public void sendMessage(@DestinationVariable Long roomId, @Payload Map<String, String> payload) {
        String username = payload.get("username");
        String content = payload.get("content");

        Users user = userRepository.findByUsername(username);
        if (user == null) {
            return;
        }

        ChatRoom room = chatRoomRepository.findById(roomId).orElse(null);
        if (room == null) {
            return;
        }

        // Verify access
        if (!chatService.canAccessRoom(user, room)) {
            return;
        }

        // Save message to database
        ChatMessage saved = chatService.saveMessage(roomId, user.getId(), username, content);

        // Broadcast to everyone subscribed to this room's topic
        messagingTemplate.convertAndSend("/topic/chat/" + roomId,
                username + ": " + content);
    }
}