/**
 * N3RD M@RKET - Prediction Market for Collectables
 * Handles welcome messages and  user greetings.
 *
 * @author Hrushi Bhatt
 */

package coms309;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
class HomePage {

    @GetMapping("/")
    public String getHomePage() {
        return "Welcome to N3RD M@RKET - Your prediction market for collectables!";
    }

    @GetMapping("/{name}")
    public String greetUser(@PathVariable String name) {
        return "Welcome to N3RD M@RKET - Your prediction market for collectables!: " + name;
    }

    @GetMapping("/about")
    public String getAboutInfo() {
        return "N3RD M@RKET: Predict prices on Comics, Trading Cards, Movies, and Games!";
    }

    @GetMapping("/price")
    public String getPrice() {
        return "PSA 10 Mega Charizard EX from the Phantasmal Flames set: Starting price $1,000,000";
    }

    @GetMapping(value = "/image", produces = "text/html")
    public String getImage() {
        return "<html><body>" +
                "<h1>N3RD M@RKET - Pokemon Predictions</h1>" +
                "<img src='https://upload.wikimedia.org/wikipedia/en/2/28/Pok%C3%A9mon_Bulbasaur_art.png' width='200'/>" +
                "<p>Will Bulbasaur be the most liked Pokemon by the end of 2026???</p>" +
                "</body></html>";
    }
}