package NerdMarket.scanning;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
public class ScanningController {

    @Autowired
    private ScanningService scanningService;

    // Frontend sends to: /app/scan
    // Backend broadcasts result to: /topic/scan-result
    @MessageMapping("/scan")
    @SendTo("/topic/scan-result")
    public ScanningResponse handleScan(ScanningRequest request) {
        return scanningService.processCardScan(request);
    }

    @CrossOrigin(origins = "*")
    @ResponseBody
    @PostMapping("/scan/debug")
    public Map<String, Object> debugScan(@RequestBody ScanningRequest request) {
        return scanningService.debugScan(request);
    }
}
