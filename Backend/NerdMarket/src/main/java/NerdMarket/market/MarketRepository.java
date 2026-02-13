//Extends JpaRepository - gives save(), findByID(), deleteByID().

package NerdMarket.market;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MarketRepository extends JpaRepository<Market, Long> {
    List<Market> findByCardType(String cardType);
}