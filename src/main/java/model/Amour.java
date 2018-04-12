package model;

public class Amour extends Adventure
{
    private int bids;

    public Amour()
    {
        this.name = "Amour"; //remove this later if needed
        this.battlePoints = 10;
        this.bids = 1;

    }

    public int GetBids()
    {
        return bids;

    }

    @Override public String toString()
    {
        return String.format("Name: " + name + ", Battle Points: " + battlePoints + ", Bids" + bids);

    }

}