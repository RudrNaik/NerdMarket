//Extends JpaRepository - gives save(), findByID(), deleteByID().

package NerdMarket.market;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MarketRepository extends JpaRepository<Market, Long> {
    List<Market> findByCardType(String cardType);
    Market findCardById(Long id);
    List<Market> findByCardNameContainingIgnoreCase(String cardName);

    List<Market> findTop10ByOrderByPriceDesc();
    List<Market> findTop10ByCardTypeOrderByPriceDesc(String cardType);
    List<Market> findByPriceGreaterThan(double price);
}