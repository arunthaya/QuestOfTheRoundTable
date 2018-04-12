package model;

import org.springframework.web.socket.WebSocketSession;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

public class Player {

    //Rethink access modifiers for all this stuff later
    private String name;
    private int shields;
    private String rank;
    private WebSocketSession session;
    private boolean eliminated = false;
    public boolean askedToBid = false;
    //    public boolean amourOnQuest = false;
//    public boolean playerInStory = false;
//    public boolean isHost = false;
//    public boolean extraCardTest = false;
//    public int hostCardsInHandBeforeQuest = 12;
    public static final int MAX_CARDS_HAND = 12;
    public ArrayList <Ally> alliesInPlay = new ArrayList <> ();
    public ArrayList <Adventure> cardsInHand = new ArrayList<>(); //reconsider visibility of this field
    public ArrayList<Adventure> discardPile = new ArrayList<>(); //reconsider visibility of this field
    ArrayList<Integer> cardsSelected = new ArrayList<>();
    public int roundBattleScore = 0;
    public static final HashMap<String, Integer> rankDictionary;
    static {
        rankDictionary = new HashMap<>();
        rankDictionary.put("Squire", 5);
        rankDictionary.put("Knight", 10);
        rankDictionary.put("Champion Knight", 20);
        rankDictionary.put("Knight of the Round", 9001);
    }

    private static int numPlayers = 0;

    public Player (String name, String rank, int shields)//modified
    {
        System.out.println("Player object created");
        this.name = name;
        this.rank = rank;
        this.shields = shields;

        //this.playerNum = numPlayers;
        numPlayers++;
        System.out.println("constructor called");
        System.out.println("numplayers is: " + numPlayers);
    }

    public Player(){

        System.out.println("constructor called");
        this.rank = "Squire";

    }


    public String getName(){
        return name;
    }

    public int getShields() {
        return shields;
    }

    public void setShields(int shields) {
        this.shields = shields;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public void setSession(WebSocketSession session){
        this.session = session;
    }

    public WebSocketSession getSession(){
        return session;
    }

    public void setEliminated(boolean eliminated){
        this.eliminated = eliminated;
    }

    public boolean getEliminatedValue(){
        return this.eliminated;
    }

}
