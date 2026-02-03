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

    @GetMapping("/data")
    public String data() {
        return "data";
    }

    @GetMapping("/Hands")
    public String Hands() {
        return "<img src='https://media4.giphy.com/media/v1.Y2lkPTc5MGI3NjExYWIzdXFsYXZ6d3F2aXVqbzJrbDlkbm84aHBra2F0NHhmejNyczdhMCZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/VZVK5WCg0A0H37s8ND/giphy.gif'>";
    }
}
