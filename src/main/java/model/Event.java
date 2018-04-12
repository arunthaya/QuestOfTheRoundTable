package model;

import java.util.ArrayList;

public class Event extends Story {

    private int shieldModifier;
    private int adventureCardModifier;
    private boolean eliminateAllies;
    private int weaponCardModifier;
    private int foeCardModifier;
    private String description;
    private String whosAffected;
    public static final ArrayList<String> playersTargeted = new ArrayList<>();
    static{
        playersTargeted.add("LowestRankAndShield");
        playersTargeted.add("All");
        playersTargeted.add("AllExceptDrawer");
        playersTargeted.add("DrawerOnly");
        playersTargeted.add("HighestRanked");
        playersTargeted.add("LowestRanked");
        playersTargeted.add("Next");
    }

    public Event(String name, int shieldModifier, int adventureCardModifier, boolean eliminiateAllies, int weaponCardModifier,
                 int foeCardModifier, String whosAffected, String description)
    {
        this.name = name;
        this.shieldModifier = shieldModifier;
        this.adventureCardModifier = adventureCardModifier;
        this.eliminateAllies = eliminiateAllies;
        this.weaponCardModifier = weaponCardModifier;
        this.foeCardModifier = foeCardModifier;
        this.description = description;
        this.whosAffected = whosAffected;
    }

    public int GetAdventureCardModifier(){
        return adventureCardModifier;
    }

    public boolean GetEliminateAllies(){
        return eliminateAllies;
    }

    public int GetWeaponCardModifier(){
        return weaponCardModifier;
    }

    public int GetFoeCardModifier(){
        return foeCardModifier;
    }

    public int GetShieldModifier(){
        return shieldModifier;
    }

    public String GetDescription(){
        return description;
    }

    public String GetWhosAffected(){
        return whosAffected;
    }

    @Override public String toString ()
    {
        return String.format("Name: " + name + ", Shield Modifier: " + shieldModifier + ", Adventure Cards Modifier: " + adventureCardModifier
                + ", Allies eliminated? " + eliminateAllies + ", Weapon Cards Modifier: " + weaponCardModifier + ", Foe Cards Modifier: " + foeCardModifier
                + "who's affected?: " + whosAffected + "\n" + description);
    }
}


