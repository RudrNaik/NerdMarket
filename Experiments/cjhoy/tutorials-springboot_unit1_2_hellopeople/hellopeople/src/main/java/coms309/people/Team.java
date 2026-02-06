package coms309.people;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Provides the Definition/Structure for the people row
 *
 * @author Vivek Bengre
 */
@Getter // Lombok Shortcut for generating getter methods (Matches variable names set ie teamName -> getteamName)
@Setter // Similarly for setters as well
@NoArgsConstructor // Default constructor
public class Team {

    private String teamName;

    private String Game;

    private String Location;

    private String Record;

//    public Person(){
//
//    }

    public Team(String teamName, String Game, String Location, String Record){
        this.teamName = teamName;
        this.Game = Game;
        this.Location = Location;
        this.Record = Record;
    }


    /**
     * Getter and Setters below are technically redundant and can be removed.
     * They will be generated from the @Getter and @Setter tags above class
     */

    public String getteamName() {
        return this.teamName;
    }

    public void setteamName(String teamName) {
        this.teamName = teamName;
    }

    public String getGame() {
        return this.Game;
    }

    public void setGame(String Game) {
        this.Game = Game;
    }

    public String getLocation() {
        return this.Location;
    }

    public void setLocation(String Location) {
        this.Location = Location;
    }

    public String getRecord() {
        return this.Record;
    }

    public void setRecord(String Record) {
        this.Record = Record;
    }

    @Override
    public String toString() {
        return teamName + " " 
               + Game + " "
               + Location + " "
               + Record;
    }
}
