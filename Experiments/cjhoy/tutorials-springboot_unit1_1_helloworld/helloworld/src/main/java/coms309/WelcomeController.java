package coms309;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
class WelcomeController {

    @GetMapping("/")
    public String welcome() {
        return "My name is cooper and I love pokemon cards ";
    }
    
    @GetMapping("/{name}")
    public String welcome(@PathVariable String name) {
        return "My name is cooper and I love pokemon cards " + name;
    }
    @GetMapping("/price")
    public String price() {
        return "Card price: $50";
    }
}
