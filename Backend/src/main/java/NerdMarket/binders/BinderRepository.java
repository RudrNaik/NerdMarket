package NerdMarket.binders;

import NerdMarket.market.Market;
import NerdMarket.users.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BinderRepository extends JpaRepository<Binders, Long> {
    List<Binders> findByUser(Users user);
    Binders findByUserAndCard(Users user, Market card);
    boolean existsByUserAndCard(Users user, Market card);
    boolean existsByUserIdAndCard_CardType(Long userId, String cardType);
}
