package model;

public class Weapon extends Adventure
{
    public Weapon(String name, int battlePoints)
    {
        this.name = name;
        this.battlePoints = battlePoints;
    }

    public void setBattlePoints(int battlePoints){
        this.battlePoints = battlePoints;
    }

    @Override
    public boolean equals(Object obj){ //Gonna get rid of this soon
        /*if(obj == null && this != null)
            return false;
        return ((Weapon)obj).name.equals(this.name); */
        return this == obj;
    }

    @Override
    public String toString()
    {
        return String.format("Name: " + name + ", Battle Points: " + battlePoints);
    }

}
