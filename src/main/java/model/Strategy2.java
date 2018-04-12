package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static com.devglan.config.Application.logIt;

public class Strategy2 extends CommonStrategy {

    @Override
    public int doIParticipateInTournament(AIPlayer player) {
        int myBattleScore = 0;

        ArrayList<Adventure> tempHand = player.cardsInHand;
        Collections.sort(tempHand, new Comparator<Adventure>() {
            @Override
            public int compare(Adventure o1, Adventure o2) {
                return o1.GetBattlePoints() - o2.GetBattlePoints();
            }
        });//sorted in ascending order

        ArrayList<String> names = new ArrayList<>();
        int cardsUsed = 0;
        for(int i = tempHand.size() - 1; i > -1; i--){
            Adventure a = tempHand.get(i);
            logIt.info("Possible card to be played is " + a.name);
            if(myBattleScore < 50){
                if(a instanceof Weapon || a instanceof  Amour) {
                    if (!names.contains(a.name)) {
                        names.add(a.name);
                        myBattleScore += a.GetBattlePoints();
                        tempHand.remove(i);
                    }
                }else if(a instanceof  Ally){
                    myBattleScore += a.GetBattlePoints();
                    player.alliesInPlay.add((Ally) tempHand.remove(i));
                }
            }else{
                logIt.info("Cards used | myBattleScore " + cardsUsed + " | " + myBattleScore);
                break;
            }
        }

        return myBattleScore;
    }

    @Override
    public int questSponsorShip(AIPlayer player){//if 0 he sponsored, if -1 he didnt
       return doISponsorAQuest(player, 40);
    }

    @Override
    public int doIParticipateInQuest(AIPlayer player) { //-1 for no, 0 for yes
        player.cardsForQuest.clear();
        //TODO clear in other strategy as well
        int gonnaJoin = -1;
        boolean foesLess = foesLessThan(player, 25);
        if(foesLess){
            boolean incrementTen = checkIncrementTen(player);
            if(incrementTen) gonnaJoin = 0;
        }

        return gonnaJoin;
    }

    public boolean checkIncrementTen(AIPlayer player){

        boolean canIncrementByTens = true;

        ArrayList<ArrayList<Adventure>> keys = new ArrayList<>();
        ArrayList<Adventure> cards = new ArrayList<>();
        cards.addAll(player.cardsInHand);
        Quest questCard = (Quest) player.theGameBoard.discardStoryDeck.get(player.theGameBoard.discardStoryDeck.size() - 1);
        int numStages = questCard.GetNumStages();

        boolean amourAvailable = true;
        for(int i = 0; i < numStages; i++){
            keys.add(new ArrayList<>());
            int amourFound = contains(cards, "Amour", questCard);
            int allyFound = contains(cards, "Ally", questCard);
            if(amourAvailable && amourFound != -1 ){
                amourAvailable = false;
                keys.get(i).add(cards.remove(amourFound));
            }else if(allyFound != -1){
                keys.get(i).add(cards.remove(allyFound));
            }
        }   
        //okay so if possible by just allies and amour we are done, otherwise we need to try weapons and 
        //whatever allies are left
        int prevBattlePoints = 0, currBattlePoints = 0;
        for(int i = 0; i < numStages; i++){
            currBattlePoints = 0;
            ArrayList<Adventure> thisKey = keys.get(i);
            if(thisKey.size() == 0){
                ArrayList<Integer> indexes = minWorkableWeapons(cards, prevBattlePoints);
                if(indexes.size() == 0){
                    canIncrementByTens = false;
                    break;
                }else{
                    //sort in descending order in minWorkableWeapons
                    for(Integer j : indexes) {
                        currBattlePoints += cards.get(j).GetBattlePoints();
                        thisKey.add(cards.remove((int)j));
                    }
                }
            }
            prevBattlePoints = currBattlePoints;
        } 

        if(canIncrementByTens){
            //remove from cardsInHand
            //set to global in AIPLayer
            for(int i = 0; i < keys.size(); i++){
                ArrayList<Adventure> thisKey = keys.get(i);
                for(Adventure a : thisKey){
                    player.cardsInHand.remove(a);
                }
            }
            player.cardsForQuest = keys;
        }    

        return canIncrementByTens;

    }

    public int contains(ArrayList<Adventure> cards, String lookingFor, Quest questCard){
        int found = -1;
        for(int i = 0; i < cards.size(); i++){
            Adventure a = cards.get(i);
            if(lookingFor.equals("Ally") && a instanceof Ally){
                int bp = a.GetBattlePoints();
                if(a.name.equals(questCard.GetLinkedFoe())){
                    bp = ((Ally) a).GetBoostedBattlePoints();
                }
                if(bp >= 10){
                    found = i;
                    break;
                }else{
                    continue;
                }
            }else if(lookingFor.equals("Amour") && a instanceof Amour){
                found = i;
                break;
            }
        }
        return found;
    }

    public ArrayList<Integer> minWorkableWeapons(ArrayList<Adventure> cards, int prevBattlePoints){
        ArrayList<Integer> indexes = new ArrayList<>();
        ArrayList<Adventure> daggers = new ArrayList<>();
        Adventure sirPercival = null;
        
        for(int i = cards.size() - 1; i > -1; i++){//remove non all weapons, daggers, sir percival
            Adventure a = cards.get(i);
            if(!(a instanceof Weapon) || a.name.equals("Dagger")){
                if(a.name.equals("Sir Percival")) sirPercival = a;
                else if(a.name.equals("Dagger")) daggers.add(a);
                cards.remove(i);
            }
        }
        Collections.sort(cards, new Comparator<Adventure>() {
            @Override
            public int compare(Adventure o1, Adventure o2) {
                return o1.GetBattlePoints() - o2.GetBattlePoints();
            }
        });//sorted in ascending order
        //if(sirPercival != null) cards.add(0, sirPercival);

        int currentScore = 0;
        ArrayList<String> nameList = new ArrayList();
        int goTo = cards.size();
        for(int i = 0; i < goTo; i++){
            Adventure a = cards.get(i);

            if(nameList.contains(a.name)){
                continue;
            }else{
                indexes.add(i);
                nameList.add(a.name);
                currentScore += a.GetBattlePoints();
            }

            if(currentScore + 5 >= (prevBattlePoints + 10)){
                if(sirPercival != null){
                    cards.add(sirPercival);
                    indexes.add(cards.size() - 1);
                    sirPercival = null;
                    currentScore += 5;
                }else if(daggers.size() > 0 && !nameList.contains("Dagger")){
                    cards.add(daggers.get(0));
                    indexes.add(cards.size() - 1);
                    nameList.add("Dagger");
                    currentScore += 5;
                }
            }
            if(currentScore >= (prevBattlePoints + 10)){
                break;
            }

        }

        if((currentScore + 10) >= (prevBattlePoints + 10) && sirPercival != null && daggers.size() > 0
             && !nameList.contains("Dagger")){
            
            cards.add(sirPercival);
            indexes.add(cards.size() - 1);
            cards.add(daggers.get(0));
            indexes.add(cards.size() - 1);

            currentScore += 10;
        }

        if(currentScore < (prevBattlePoints + 10)) indexes.clear();

        if(indexes.size() > 0){
            Collections.sort(indexes, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o2 - o1;
            }
        });//sorted in descending order
        }

        return indexes;
    }

    @Override
    public int nextBid(AIPlayer player, int currentMaxBid) { //return -1 if not gonna bid higher
        //TODO reset round number bidding ( to 1 ) after test is done for all AI players and reset global arraylist
        //done here must be done in server as well in case AI wins

        int cardsToBid = 0;
        if(player.roundNumberBidding == 1){
            player.cardsInHand.addAll(player.cardsForQuest.remove(player.cardsForQuest.size() - 1));
            for(int i = player.cardsInHand.size() - 1; i > -1; i--){ //kinda pointless cause removing by object later
                Adventure a = player.cardsInHand.get(i);
                if(a instanceof Foe && a.GetBattlePoints() < 25){
                    cardsToBid++;
                    player.discardIfTestWon.add(a);
                }
            }
        }else if(player.roundNumberBidding == 2){
            cardsToBid = player.discardIfTestWon.size();
            ArrayList<String> nameList = new ArrayList<>();
            for(int i = 0; i < player.cardsInHand.size(); i++){
                Adventure a = player.cardsInHand.get(i);
                if(a instanceof Weapon && nameList.contains(a.name)){//so the first one will never be added
                    cardsToBid++;
                    player.discardIfTestWon.add(a);
                }else{
                    nameList.add(a.name);
                }
            }
        }

        if(cardsToBid > currentMaxBid){
                return cardsToBid;
        }else{
            player.roundNumberBidding = 1;
            player.discardIfTestWon.clear();
            return -1;
        }
    }
}
