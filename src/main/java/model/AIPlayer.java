package model;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import static com.devglan.config.Application.logIt;

public class AIPlayer extends Player {

    public AIStrategy strategy;
    //public ArrayList<Pair<ArrayList<Adventure>, Integer>> stagesAsHost = new ArrayList<>();
    public LinkedHashMap<ArrayList<Adventure>, Integer> stagesAsHost = new LinkedHashMap<>();
    public ArrayList<ArrayList<Adventure>> cardsForQuest = new ArrayList<>();
    public ArrayList<Adventure> discardIfTestWon = new ArrayList();
    public GameBoard theGameBoard;
    int positionInPlayerList;
    int roundNumberBidding = 1;

    public AIPlayer(String name, String rank, int shields, String strats , GameBoard theGameboard, int positionInPlayerList){
        super(name, rank, shields);
        this.theGameBoard = theGameboard;
        //TODO set strategy here
        //TODO set positionInPlayerList here
        logIt.info("AI Player done creation");
    }

    public String toString(){
        return " Name : " + this.getName() + " ,rank: " + this.getRank() + " ,shields: " + this.getShields();
    }
}
