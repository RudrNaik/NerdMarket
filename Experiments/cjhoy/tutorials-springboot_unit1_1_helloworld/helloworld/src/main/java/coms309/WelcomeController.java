package coms309;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
class WelcomeController {

    @GetMapping("/")
    public String welcome() {
        return "Welcome to the N3RD M@RKET ";
    }
    
    @GetMapping("/cooper")
    public String welcome(@PathVariable String name) {
        return "My name is cooper and I love pokemon cards " + name;
    }
    @GetMapping("/price")
    public String price() {
        return "Card price: $50";
    }

    @GetMapping("/image")
    public String image() {
        return "<img src='https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/25.png'>";
    }
}
