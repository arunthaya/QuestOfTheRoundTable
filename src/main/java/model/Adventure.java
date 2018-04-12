package model;

abstract public class Adventure extends Card {
    protected String name;
    protected int battlePoints;

    public String GetName() {
        return name;
    }

    public int GetBattlePoints() {
        return battlePoints;
    }
}
