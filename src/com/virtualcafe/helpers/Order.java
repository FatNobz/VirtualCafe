package com.virtualcafe.helpers;

public class Order {
    private int teaInWaitingArea = 0;
    private int coffeeInWaitingArea = 0;
    private int teaInBrewing = 0;
    private int coffeeInBrewing = 0;
    private int teaInTray = 0;
    private int coffeeInTray = 0;


    //Adds tea and coffee quantities to the waiting area.
    public void addToWaitingArea(int teaQuantity, int coffeeQuantity) {
        this.teaInWaitingArea += teaQuantity;
        this.coffeeInWaitingArea += coffeeQuantity;
    }

    /**
     * Moves a specified quantity of tea from the waiting area to brewing.
     * Throws an exception if the quantity exceeds the amount available.
     */
    public void moveTeaToBrewing(int teaQuantity) {
        if (teaQuantity > teaInWaitingArea) {
            throw new IllegalArgumentException("Not enough tea in waiting area.");
        }
        this.teaInWaitingArea -= teaQuantity;
        this.teaInBrewing += teaQuantity;
    }

    /**
     * Moves a specified quantity of coffee from the waiting area to brewing.
     * Throws an exception if the quantity exceeds the amount available.
     */
    public void moveCoffeeToBrewing(int coffeeQuantity) {
        if (coffeeQuantity > coffeeInWaitingArea) {
            throw new IllegalArgumentException("Not enough coffee in waiting area.");
        }
        this.coffeeInWaitingArea -= coffeeQuantity;
        this.coffeeInBrewing += coffeeQuantity;
    }

    /**
     * Moves a specified quantity of tea from brewing to the tray.
     * Throws an exception if the quantity exceeds the amount brewing.
     */
    public void moveTeaToTray(int teaQuantity) {
        if (teaQuantity > teaInBrewing) {
            throw new IllegalArgumentException("Not enough tea in brewing.");
        }
        this.teaInBrewing -= teaQuantity;
        this.teaInTray += teaQuantity;
    }

    /**
     * Moves a specified quantity of coffee from brewing to the tray.
     * Throws an exception if the quantity exceeds the amount brewing.
     */
    public void moveCoffeeToTray(int coffeeQuantity) {
        if (coffeeQuantity > coffeeInBrewing) {
            throw new IllegalArgumentException("Not enough coffee in brewing.");
        }
        this.coffeeInBrewing -= coffeeQuantity;
        this.coffeeInTray += coffeeQuantity;
    }

    // Getters to retrieve the current quantities of tea and coffee in various stages.

    public int getTeaInWaitingArea() {
        return teaInWaitingArea;
    }

    public int getCoffeeInWaitingArea() {
        return coffeeInWaitingArea;
    }

    public int getTeaInBrewing() {
        return teaInBrewing;
    }

    public int getCoffeeInBrewing() {
        return coffeeInBrewing;
    }

    public int getTeaInTray() {
        return teaInTray;
    }

    public int getCoffeeInTray() {
        return coffeeInTray;
    }

    /**
     * Checks if the order is ready for collection.
     * The order is ready only when all items are in the tray.
     */
    public boolean isReadyForCollection() {
        return teaInWaitingArea == 0 && coffeeInWaitingArea == 0
                && teaInBrewing == 0 && coffeeInBrewing == 0;
    }


    //Provides a summary of tea and coffee quantities in the waiting area.
    public String getWaitingDetails() {
        return teaInWaitingArea + " tea(s), " + coffeeInWaitingArea + " coffee(s)";
    }


    //Provides a summary of tea and coffee quantities currently brewing.
    public String getBrewingDetails() {
        return teaInBrewing + " tea(s), " + coffeeInBrewing + " coffee(s)";
    }


    //Provides a summary of tea and coffee quantities in the tray.
    public String getTrayDetails() {
        return teaInTray + " tea(s), " + coffeeInTray + " coffee(s)";
    }
}
