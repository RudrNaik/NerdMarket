package NerdMarket.scanning;

import NerdMarket.market.Market;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScanningMatch {
    private Market card;
    private int confidence; // 0-100
}
