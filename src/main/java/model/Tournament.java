package model;

public class Tournament extends Story {
    protected int shieldModifier;

    public Tournament (String name, int shieldModifier)
    {
        this.name = name;
        this.shieldModifier = shieldModifier;
    }

    public int GetShieldModifier(){
        return shieldModifier;
    }

    @Override public String toString ()
    {
        return String.format ("Tournament at " + name + ", bonus shields " + shieldModifier);
    }

}