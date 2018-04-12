package model;

public class Ally extends Adventure
{
    private int bids;
    private int boostedBattlePoints;

    public Ally(String name, int battlePoints, int bids, int boostedBattlePoints)
    {
        this.name = name;
        this.battlePoints = battlePoints;
        this.bids = bids;
        this.boostedBattlePoints = boostedBattlePoints;
    }
    public int GetBids()
    {
        return bids;
    }

    public int GetBoostedBattlePoints(){
        return boostedBattlePoints;
    }

    @Override
    public String toString()
    {
        return String.format("Name: " + name + ", Battle Points: " + battlePoints + ", Bids " + bids);
    }
}
