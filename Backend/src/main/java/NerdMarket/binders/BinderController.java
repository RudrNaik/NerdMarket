package NerdMarket.binders;

import NerdMarket.users.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class BinderController {

    @Autowired
    private BinderService binderService;

    @GetMapping("/{username}/binder")
    public ResponseEntity<?> getBinder(@PathVariable String username) {
        try {
            Users user = binderService.requireUser(username);
            List<Binders> entries = binderService.getBinderForUser(user);

            Map<String, Object> userInfo = new LinkedHashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());

            List<Map<String, Object>> binderList = new ArrayList<>();
            for (Binders entry : entries) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("added_at", entry.getAddedAt());
                item.put("card", entry.getCard());
                binderList.add(item);
            }

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("user", userInfo);
            response.put("binder", binderList);

            return ResponseEntity.ok(response);
        } catch (BinderService.UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"message\":\"" + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/{username}/binder")
    public ResponseEntity<?> addCard(@PathVariable String username, @RequestBody Map<String, Object> body) {
        if (body == null || !body.containsKey("card_id")) {
            return ResponseEntity.badRequest().body("{\"message\":\"card_id is required\"}");
        }

        Long cardId;
        try {
            cardId = Long.parseLong(body.get("card_id").toString());
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("{\"message\":\"card_id must be a valid number\"}");
        }

        try {
            binderService.addCard(username, cardId);
            return ResponseEntity.ok("{\"message\":\"success\"}");
        } catch (BinderService.UserNotFoundException | BinderService.CardNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"message\":\"" + e.getMessage() + "\"}");
        } catch (BinderService.CardAlreadyInBinderException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("{\"message\":\"" + e.getMessage() + "\"}");
        }
    }

    @DeleteMapping("/{username}/binder/{cardId}")
    public ResponseEntity<?> removeCard(@PathVariable String username, @PathVariable Long cardId) {
        try {
            binderService.removeCard(username, cardId);
            return ResponseEntity.ok("{\"message\":\"success\"}");
        } catch (BinderService.UserNotFoundException | BinderService.CardNotFoundException | BinderService.BinderEntryNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{\"message\":\"" + e.getMessage() + "\"}");
        }
    }
}
