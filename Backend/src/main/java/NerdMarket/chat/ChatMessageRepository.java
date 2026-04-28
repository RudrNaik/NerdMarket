package NerdMarket.chat;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByChatRoomIdOrderBySentAtAsc(Long chatRoomId);

    void deleteAllByChatRoomId(Long chatRoomId);
}