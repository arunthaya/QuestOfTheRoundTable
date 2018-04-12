package model;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static com.devglan.config.Application.logIt;

abstract public class CommonStrategy implements AIStrategy {

    @Override
    public int doISponsorAQuest(AIPlayer player, int maxStageBP) {
        int doISponsor = -1;

        boolean opb = otherPlayerBenefits(player);
        GameBoard board = player.theGameBoard;

        if(!opb) { //then we will try to sponsor
            if(player.theGameBoard.canSponsor(player)){
                logIt.info(player.getName() + " (is AI) will and can sponsor");
                Test hasTest = hasTestCard(player);
                //Set up from last to first, then reverse
                //TODO not possible with map, tell Arun first entry is last stage
                Quest quest = (Quest) board.discardStoryDeck.get(board.discardStoryDeck.size() - 1);
                setUpStages(maxStageBP, quest.GetNumStages(), player, hasTest);
                doISponsor = 0;
            }else{
                logIt.info(player.getName() + " (is AI) and is unable to sponsor based on cards in hand, but did want to");
            }
        }else{
            logIt.info(player.getName() + " (is AI) has decided not to sponsor the quest");
        }

        return doISponsor;
    }

    public boolean foesLessThan(AIPlayer player, int battlePointsNumber){
        boolean foesLessThan = false;
        int counter = 0;
        for(int i = 0; i < player.cardsInHand.size(); i++){
            Adventure a = player.cardsInHand.get(i);
            if(a instanceof Foe && a.battlePoints < battlePointsNumber){
                counter++;
            }
            if(counter >= 2) {
                foesLessThan = true;
                break;
            }
        }

        return foesLessThan;
    }


    public boolean otherPlayerBenefits(AIPlayer player){//pass in null here to check self
        boolean opb = false;

        GameBoard board = player.theGameBoard;
        int maxExtraShields = 0;
        Story story = board.discardStoryDeck.get(board.discardStoryDeck.size() - 1);
        if(story instanceof  Tournament){
            maxExtraShields += board.playerList.size();
            maxExtraShields += ((Tournament) story).shieldModifier;
        }else if(story instanceof Quest){
            maxExtraShields += ((Quest) story).GetNumStages();
        }else{
            logIt.error("Critical enter 9001, invalid type");
        }
        for(Player p : board.playerList){
            if(p != player ){ //compare objects in memory
                if(p.getRank().equals("Squire")){
                    if(p.getShields() + maxExtraShields >= 5){
                        opb = true;
                        break;
                    }
                }else if(p.getRank().equals("Knight")){
                    if(p.getShields() + maxExtraShields >= 7){
                        opb = true;
                        break;
                    }
                }else if(p.getRank().equals("Champion Knight")){
                    if(p.getShields() + maxExtraShields >= 10){
                        opb = true;
                        break;
                    }
                }else{
                    logIt.error("Critical error 9001, invalid rank");
                }
            }
        }

        return opb;
    }


    public Test hasTestCard(AIPlayer player){
        Test test = null;
        for(int i = player.cardsInHand.size() - 1; i > -1; i++){
            Adventure a = player.cardsInHand.get(i);
            if(a instanceof  Test){
                test = (Test) a;
                break;
            }
        }
        return test;
    }

    public void setUpStages(int maxStageBP, int numStages, AIPlayer player, Test test){
        player.stagesAsHost.clear();
        ArrayList<ArrayList<Adventure>>  keys = new ArrayList<>();
        for(int i = 0; i < numStages; i++){
            keys.add(new ArrayList<Adventure>());
        }

        Collections.sort(player.cardsInHand, new Comparator<Adventure>() {
            @Override
            public int compare(Adventure o1, Adventure o2) {
                return o1.GetBattlePoints() - o2.GetBattlePoints();
            }//sort in ascending order
        });

        ArrayList<Foe> foeList = new ArrayList<>();
        ArrayList<Weapon> weaponListDuplicate = new ArrayList<>();
        ArrayList<Weapon> weaponList = new ArrayList<>();
        ArrayList<String> names = new ArrayList<>();
        for(Adventure a : player.cardsInHand){
            if(a instanceof  Foe){
                foeList.add((Foe) a);
            }else if(a instanceof  Weapon){
                if(names.contains(a.name)){
                    weaponListDuplicate.add((Weapon)a);
                }
                weaponList.add((Weapon)a);
                names.add(a.name);
            }
        }

        names.clear(); //TODO is this bit pointless? this line and below for loop
        for(int i = 0; i < weaponListDuplicate.size(); i++){
            names.add(weaponListDuplicate.get(i).name);
        }

        weaponList = singlesOnly(weaponList, weaponListDuplicate); //this one is still sorted
        Collections.sort(weaponListDuplicate, new Comparator<Weapon>() {
            @Override
            public int compare(Weapon o1, Weapon o2) {
                return o1.battlePoints - o2.battlePoints;
            }
        });
        //Now we have a duplicates and singles weapons lists

        for(int i = keys.size() - 1; i > -1; i--){
            ArrayList<Adventure> ala = keys.get(i);
            if(i == keys.size() - 1){
                ala.add(foeList.remove(foeList.size() - 1));
            }else if(i == keys.size() - 2 && test != null){
                ala.add(test);
            }else{
                ala.add(foeList.remove(0));
            }
        }

        //TODO mentions weakness in cockumentation, no weapons or only duplicate weapons are allowed for these foes so they may have the same bp
        //if test battle points was -1
        ArrayList<Adventure> finalStage = keys.get(keys.size() - 1);
        int tempBP = finalStage.get(0).GetBattlePoints(); //for the final stage
        while(tempBP <= maxStageBP){
            if(weaponList.size() < 1){
                break;
            }
            Weapon w = weaponList.remove(weaponList.size() - 1);
            tempBP += w.GetBattlePoints();
            finalStage.add(w);
        }

        if(maxStageBP == 40){//strat 2
            while(tempBP <= maxStageBP){ //bit of duplicate code here, refactor after
                if(weaponListDuplicate.size() < 1){
                    break;
                }
                Weapon w = weaponListDuplicate.remove(weaponList.size() - 1);
                tempBP += w.GetBattlePoints();
                finalStage.add(w);
            }
        }else{//strat 1

        }

        setAndRemove(keys, player);
    }

    public ArrayList<Weapon> singlesOnly(ArrayList<Weapon> weaponList, ArrayList<Weapon> duplicates){
        for(int i = 0; i < duplicates.size(); i++){
            String name = duplicates.get(i).name;
            for(int j = weaponList.size() - 1; j > -1; j--){
                if(weaponList.get(j).name.equals(name)){
                    Weapon w = weaponList.remove(j);
                    if(!duplicates.contains(w)){
                        duplicates.add(w);
                    }
                }
            }
        }
        return weaponList;
    }

    public void setAndRemove( ArrayList<ArrayList<Adventure>>  keys, AIPlayer player){
        for(int i = 0; i < keys.size(); i++){
            int battlePoints = -1;
            ArrayList<Adventure> ala  = keys.get(i);
            for(Adventure a : ala){
                if(a instanceof  Test){
                    if(ala.size() > 1) logIt.error("Critical error 9001");
                }else{
                    battlePoints += 1;
                    battlePoints += a.GetBattlePoints();
                }
                player.cardsInHand.remove(a);
            }
            player.stagesAsHost.put(ala, battlePoints);
        }
    }

    @Override
    public void discardAfterWinningTest(AIPlayer player) {
        for(int i = 0; i < player.discardIfTestWon.size(); i++){
            Adventure a = player.discardIfTestWon.get(i);
            boolean successfulRemoval = player.cardsInHand.remove(a);
            if(!successfulRemoval){
                logIt.error("CRITICAL ERROR 9001 in discardAfterWinningTest (AI)");
                return;
            }
        }
    }

    @Override
    public void discardIfHandFull(){

    }
}
