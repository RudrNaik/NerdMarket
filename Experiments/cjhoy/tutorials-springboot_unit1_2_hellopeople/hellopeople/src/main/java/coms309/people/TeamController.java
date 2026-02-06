package coms309.people;

import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Controller used to showcase Create and Read from a LIST
 *
 * @author Vivek Bengre
 */

@RestController
public class TeamController {

    // Note that there is only ONE instance of TeamController in
    // Springboot system.
    HashMap<String, Team> teamList = new HashMap<>();

    //CRUDL (create/read/update/delete/list)
    // use POST, GET, PUT, DELETE, GET methods for CRUDL

    // THIS IS THE LIST OPERATION
    @GetMapping("/teams")
    public HashMap<String, Team> getAllTeams() {
        return teamList;
    }

    // THIS IS THE CREATE OPERATION
    @PostMapping("/teams")
    public String createTeam(@RequestBody Team team) {
        System.out.println(team);
        teamList.put(team.getteamName(), team);
        String s = "New team " + team.getteamName() + " Saved";
        return s;
    }

    // THIS IS THE READ OPERATION
    @GetMapping("/teams/{teamName}")
    public Team getTeam(@PathVariable String teamName) {
        Team t = teamList.get(teamName);
        return t;
    }

    // THIS IS A GET METHOD
    @GetMapping("/teams/contains")
    public List<Team> getTeamByParam(@RequestParam("name") String name) {
        List<Team> res = new ArrayList<>();
        for (Team t : teamList.values()) {
            if (t.getteamName().contains(name) || t.getGame().contains(name))
                res.add(t);
        }
        return res;
    }

    // THIS IS THE UPDATE OPERATION
    @PutMapping("/teams/{teamName}")
    public Team updateTeam(@PathVariable String teamName, @RequestBody Team t) {
        teamList.replace(teamName, t);
        return teamList.get(teamName);
    }

    // THIS IS THE DELETE OPERATION
    @DeleteMapping("/teams/{teamName}")
    public HashMap<String, Team> deleteTeam(@PathVariable String teamName) {
        teamList.remove(teamName);
        return teamList;
    }
}