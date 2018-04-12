package model;

public class Quest extends Story {

    private int numStages;
    private String linkedFoe;
    private String linkedAlly;

    public Quest (String name, int numStages, String linkedFoe, String linkedAlly)
    {
        this.name = name;
        this.numStages = numStages;
        this.linkedFoe = linkedFoe;
        this.linkedAlly = linkedAlly;
    }

    public int GetNumStages(){
        return numStages;
    }

    public String GetLinkedFoe(){
        return linkedFoe;
    }

    public String GetLinkedAlly(){
        return linkedAlly;
    }

    @Override public String toString ()
    {
        return String.format("Name: " + name + ", Number of Stages: " + numStages + ", Linked Foe: " + linkedFoe + ", Linked Ally " + linkedAlly);
    }
}
