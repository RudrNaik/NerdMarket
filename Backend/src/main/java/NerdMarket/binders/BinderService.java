package NerdMarket.binders;

import NerdMarket.market.Market;
import NerdMarket.market.MarketRepository;
import NerdMarket.users.UserRepository;
import NerdMarket.users.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BinderService {

    @Autowired
    private BinderRepository binderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MarketRepository marketRepository;

    public Users requireUser(String username) {
        Users user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UserNotFoundException("User not found: " + username);
        }
        return user;
    }

    public Market requireCard(Long cardId) {
        Market card = marketRepository.findCardById(cardId);
        if (card == null) {
            throw new CardNotFoundException("Card not found: " + cardId);
        }
        return card;
    }

    public List<Binders> getBinder(String username) {
        Users user = requireUser(username);
        return binderRepository.findByUser(user);
    }

    public List<Binders> getBinderForUser(Users user) {
        return binderRepository.findByUser(user);
    }

    public Binders addCard(String username, Long cardId) {
        Users user = requireUser(username);
        Market card = requireCard(cardId);

        if (binderRepository.existsByUserAndCard(user, card)) {
            throw new CardAlreadyInBinderException("Card already in binder");
        }

        Binders entry = new Binders();
        entry.setUser(user);
        entry.setCard(card);
        return binderRepository.save(entry);
    }

    public void removeCard(String username, Long cardId) {
        Users user = requireUser(username);
        Market card = requireCard(cardId);

        Binders entry = binderRepository.findByUserAndCard(user, card);
        if (entry == null) {
            throw new BinderEntryNotFoundException("Card not in binder");
        }
        binderRepository.delete(entry);
    }

    // --- Exceptions ---

    public static class UserNotFoundException extends RuntimeException {
        public UserNotFoundException(String message) { super(message); }
    }

    public static class CardNotFoundException extends RuntimeException {
        public CardNotFoundException(String message) { super(message); }
    }

    public static class CardAlreadyInBinderException extends RuntimeException {
        public CardAlreadyInBinderException(String message) { super(message); }
    }

    public static class BinderEntryNotFoundException extends RuntimeException {
        public BinderEntryNotFoundException(String message) { super(message); }
    }
}
