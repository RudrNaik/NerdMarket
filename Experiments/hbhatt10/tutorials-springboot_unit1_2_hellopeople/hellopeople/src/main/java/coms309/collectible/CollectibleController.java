package coms309.collectible;

import org.springframework.web.bind.annotation.*;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Controller used to showcase Create and Read from a LIST
 *
 * @author Vivek Bengre
 */

//ORIGINALLY PersonController.java

@RestController
public class CollectibleController {

    // Note that there is only ONE instance of PeopleController in 
    // Springboot system.
    HashMap<String, Collectible> collectiblesList = new  HashMap<>();

    //CRUDL (create/read/update/delete/list)
    // use POST, GET, PUT, DELETE, GET methods for CRUDL

    // THIS IS THE LIST OPERATION
    // gets all the people in the list and returns it in JSON format
    // This controller takes no input. 
    // Springboot automatically converts the list to JSON format 
    // in this case because of @ResponseBody
    // Note: To LIST, we use the GET method
    @GetMapping("/collectibles")
    public  HashMap<String,Collectible> getAllCollectibles() {
        return collectiblesList;
    }

    // THIS IS THE CREATE OPERATION
    // springboot automatically converts JSON input into a person object and 
    // the method below enters it into the list.
    // It returns a string message in THIS example.
    // Note: To CREATE we use POST method
    @PostMapping("/collectibles")
    public  String createCollectible(@RequestBody Collectible collectible) {
        System.out.println(collectible);
        collectiblesList.put(collectible.getItemName(), collectible);
        String s = "New collectible "+ collectible.getItemName() + " Saved";
        return s;
        //public  ResponseEntity<Map<String, String>>  //unused
        // createPerson(@RequestBody Person person) { // unused
        //Map <String, String> body = new HashMap<>();// unused
        //body.put("message", s); // unused
        //ResponseEntity<>(body, HttpStatus.OK); // unused
    }

    // THIS IS THE READ OPERATION
    // Springboot gets the PATHVARIABLE from the URL
    // We extract the person from the HashMap.
    // springboot automatically converts Person to JSON format when we return it
    // Note: To READ we use GET method
    @GetMapping("/collectibles/{itemName}")
    public Collectible getCollectible(@PathVariable String itemName) {
        Collectible p = collectiblesList.get(itemName);
        return p;
    }

    // THIS IS A GET METHOD
    // RequestParam is expected from the request under the key "name"
    // returns all names that contains value passed to the key "name"
    @GetMapping("/collectibles/contains")
    public List<Collectible> getCollectibles(@RequestParam("name") String name) {
        List<Collectible> res = new ArrayList<>();
        for (Collectible p : collectiblesList.values()) {
            if (p.getItemName().contains(name) || p.getCategory().contains(name))
                res.add(p);
        }
        return res;
    }

    // THIS IS THE UPDATE OPERATION
    // We extract the person from the HashMap and modify it.
    // Springboot automatically converts the Person to JSON format
    // Springboot gets the PATHVARIABLE from the URL
    // Here we are returning what we sent to the method
    // Note: To UPDATE we use PUT method
    @PutMapping("/collectibles/{itemName}")
    public Collectible updateCollectible(@PathVariable String itemName, @RequestBody Collectible collectible) {
        collectiblesList.replace(itemName, collectible);
        return collectiblesList.get(itemName);
    }


    // THIS IS THE DELETE OPERATION
    // Springboot gets the PATHVARIABLE from the URL
    // We return the entire list -- converted to JSON
    // Note: To DELETE we use delete method
    
    @DeleteMapping("/collectibles/{itemName}")
    public HashMap<String, Collectible> deleteCollectible(@PathVariable String itemName) {
        collectiblesList.remove(itemName);
        return collectiblesList;
    }
} // end of people controller

