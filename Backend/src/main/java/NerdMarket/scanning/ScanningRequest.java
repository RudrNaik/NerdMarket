package NerdMarket.scanning;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScanningRequest {
    private String imageBase64;   // base64-encoded card image from the frontend
    private String cardNameOcr;   // card name extracted by frontend OCR (Tesseract)
}