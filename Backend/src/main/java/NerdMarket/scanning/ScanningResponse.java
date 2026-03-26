package NerdMarket.scanning;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScanningResponse {
    private boolean success;
    private String message;
    private List<ScanningMatch> cards;  // top match first; multiple if scores are too close to distinguish
}
