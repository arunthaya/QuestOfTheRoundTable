package model;

public class Test extends Adventure
{
    private int minBids;

    public Test(String name, int minBids)
    {
        this.name = name;
        this.minBids = minBids;
    }

    public int GetMinBids(){
        return minBids;
    }

    @Override public String toString()
    {
        return String.format("Name: " + name + ", minimum bids: " + minBids);
    }


}
