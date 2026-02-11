package coms309.collectible;



import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Provides the Definition/Structure for the people row
 *
 * @author Vivek Bengre
 */

//ORIGINALLY Person.java

@Getter // Lombok Shortcut for generating getter methods (Matches variable names set ie firstName -> getFirstName)
@Setter // Similarly for setters as well
@NoArgsConstructor // Default constructor
public class Collectible {

    private String itemName; //Specific Names: "Mega Charizard EX, Intersteller by Christopher Nolan, etc.."

    private String category; //Like "Trading Card, Comics, Hot Wheels, Movies, etc.."

    private Double currentPrice;

    private Double predictedPrice;

//    public Person(){
//
//    }

    public Collectible(String itemName, String category, double currentPrice, double predictedPrice){
        this.itemName = itemName;
        this.category = category;
        this.currentPrice = currentPrice;
        this.predictedPrice = predictedPrice;
    }


    /**
     * Getter and Setters below are technically redundant and can be removed.
     * They will be generated from the @Getter and @Setter tags above class
     */

    public String getItemName() {
        return this.itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getCategory() {
        return this.category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getCurrentPrice() {
        return this.currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public double getPredictedPrice() {
        return this.predictedPrice;
    }

    public void setPredictedPrice(double predictedPrice) {
        this.predictedPrice = predictedPrice;
    }

    @Override
    public String toString() {
        return itemName + " "
               + category + " "
               + currentPrice + " "
               + predictedPrice;
    }
}
