package NerdMarket.chat;

import NerdMarket.binders.BinderRepository;
import NerdMarket.users.UserRepository;
import NerdMarket.users.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChatService {

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BinderRepository binderRepository;

    //Get all chat rooms a user has access to based on their role and binder
    public List<ChatRoom> getAccessibleRooms(Long userId) {
        Users user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        List<ChatRoom> accessibleRooms = new ArrayList<>();
        List<ChatRoom> allRooms = chatRoomRepository.findAll();
        for (ChatRoom room : allRooms) {
            if (canAccessRoom(user, room)) {
                accessibleRooms.add(room);
            }
        }
        return accessibleRooms;
    }

    //Check if a user can access a specific chat room
    public boolean canAccessRoom(Users user, ChatRoom room) {

        if (user.isAdmin()) {
            return true;
        }

        String roomType = room.getType();
        if ("ADMIN".equals(roomType)) { // Admin rooms
            return false;
        }

        if ("MODERATOR".equals(roomType)) { // Moderator rooms
            return user.isModerator();
        }

        // Card type rooms
        if ("CARD_TYPE".equals(roomType)) {
            return binderRepository.existsByUserIdAndCard_CardType(user.getId(), room.getCardType());
        }
        return false;
    }

    //Moderator creates a new chat room
    public ChatRoom createRoom(Long userId, String name, String type, String cardType) {
        Users user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isModerator() && !user.isAdmin()) {
            throw new RuntimeException("Only moderators or admins can create chat rooms");
        }

        if (chatRoomRepository.findByName(name) != null) {
            throw new RuntimeException("A chat room with that name already exists");
        }

        ChatRoom room = new ChatRoom();
        room.setName(name);
        room.setType(type);
        room.setCardType(cardType);
        chatRoomRepository.save(room);

        return room;
    }

    //Moderator deletes a chat room and all its messages
    @Transactional
    public void deleteRoom(Long userId, Long roomId) {
        Users user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isModerator() && !user.isAdmin()) {
            throw new RuntimeException("Only moderators or admins can delete chat rooms");
        }

        if (!chatRoomRepository.existsById(roomId)) {
            throw new RuntimeException("Chat room not found");
        }

        chatMessageRepository.deleteAllByChatRoomId(roomId);
        chatRoomRepository.deleteById(roomId);
    }

    //Get chat history for a room
    public List<ChatMessage> getRoomHistory(Long roomId) {
        if (!chatRoomRepository.existsById(roomId)) {
            throw new RuntimeException("Chat room not found");
        }
        return chatMessageRepository.findByChatRoomIdOrderBySentAtAsc(roomId);
    }

    //Save a chat message to the database
    public ChatMessage saveMessage(Long roomId, Long userId, String username, String content) {
        ChatRoom room = chatRoomRepository.findById(roomId).orElseThrow(() -> new RuntimeException("Chat room not found"));

        Users user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        ChatMessage message = new ChatMessage();
        message.setChatRoom(room);
        message.setUser(user);
        message.setUsername(username);
        message.setContent(content);
        chatMessageRepository.save(message);

        return message;
    }

    //Moderator deletes a message
    public void deleteMessage(Long userId, Long messageId) {
        Users user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isModerator() && !user.isAdmin()) {
            throw new RuntimeException("Only moderators or admins can delete messages");
        }

        if (!chatMessageRepository.existsById(messageId)) {
            throw new RuntimeException("Message not found");
        }
        chatMessageRepository.deleteById(messageId);
    }
}