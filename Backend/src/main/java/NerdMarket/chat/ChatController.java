package NerdMarket.chat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Chat", description = "Chat rooms and messaging endpoints")
@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Operation(summary = "Get all accessible chat rooms for a user")
    @GetMapping("/rooms/{userId}")
    public ResponseEntity<?> getAccessibleRooms(@PathVariable Long userId) {
        try {
            List<ChatRoom> rooms = chatService.getAccessibleRooms(userId);
            return ResponseEntity.ok(rooms);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Create a new chat room (moderator or admin only)")
    @PostMapping("/rooms")
    public ResponseEntity<?> createRoom(@RequestBody Map<String, Object> body) {
        try {
            Long userId = Long.valueOf(body.get("userId").toString());
            String name = (String) body.get("name");
            String type = (String) body.get("type");
            String cardType = body.containsKey("cardType") ? (String) body.get("cardType") : null;

            ChatRoom room = chatService.createRoom(userId, name, type, cardType);
            return ResponseEntity.ok(room);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Delete a chat room (moderator or admin only)")
    @DeleteMapping("/rooms/{roomId}")
    public ResponseEntity<?> deleteRoom(@PathVariable Long roomId, @RequestParam Long userId) {
        try {
            chatService.deleteRoom(userId, roomId);
            return ResponseEntity.ok("Chat room deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Get chat message history for a room")
    @GetMapping("/rooms/{roomId}/history")
    public ResponseEntity<?> getRoomHistory(@PathVariable Long roomId) {
        try {
            List<ChatMessage> messages = chatService.getRoomHistory(roomId);
            return ResponseEntity.ok(messages);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Delete a chat message (moderator or admin only)")
    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<?> deleteMessage(@PathVariable Long messageId, @RequestParam Long userId) {
        try {
            chatService.deleteMessage(userId, messageId);
            return ResponseEntity.ok("Message deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}