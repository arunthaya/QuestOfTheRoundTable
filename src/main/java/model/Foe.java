package model;

public class Foe extends Adventure
{
    private int boostedBattlePoints;

    public Foe(String name, int battlePoints, int boostedBattlePoints)
    {
        this.name = name;
        this.battlePoints = battlePoints;
        this.boostedBattlePoints = boostedBattlePoints;

    }

    public int GetBoostedBattlePoints(){
        return this.boostedBattlePoints;
    }

    @Override
    public String toString()
    {
        return String.format("Name: " + name + ", Battle Points: " + battlePoints);

    }


}
