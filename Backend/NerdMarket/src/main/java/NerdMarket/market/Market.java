//This file will map to the Market table in MySQL.
//Will define fields such as cardType, cardName, Set, Rarity, and Price

package NerdMarket.market;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Market {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cardType;        //So here would be like Pokemon, MTG, YuGiOH, Baseball, etc..
    private String cardName;        //This is the name of it: "Charizard EX"
    private String cardSet;         //Special set: Pokemon > Surging Sparks, Prismatic Evolution, etc..
    private String cardRarity;      //EX: Pokemon > Holo, Illustration Rare, etc..
    private double price;           //Current Value (could add recent change in value, etc..)
}