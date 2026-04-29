package NerdMarket.chat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ChatDataInitializer implements CommandLineRunner {

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Override
    public void run(String... args) {
        createRoomIfNotExists("Pokemon Chat", "CARD_TYPE", "Pokemon");
        createRoomIfNotExists("MTG Chat", "CARD_TYPE", "MTG");
        createRoomIfNotExists("Yu-Gi-Oh Chat", "CARD_TYPE", "YuGiOh");
        createRoomIfNotExists("Moderator Chat", "MODERATOR", null);
        createRoomIfNotExists("Admin Chat", "ADMIN", null);
    }

    private void createRoomIfNotExists(String name, String type, String cardType) {
        if (chatRoomRepository.findByName(name) == null) {
            ChatRoom room = new ChatRoom();
            room.setName(name);
            room.setType(type);
            room.setCardType(cardType);
            chatRoomRepository.save(room);
        }
    }
}