package model;

import com.devglan.config.SocketHandler;
import static com.devglan.config.Application.logIt;
//import com.sun.org.apache.xpath.internal.operations.Bool;
import javafx.util.Pair;
//import sun.awt.image.ImageWatched;

//import java.io.IOException;
//import java.lang.reflect.Array;
import java.util.*;


public class GameBoard {

    public SocketHandler controller;

    public ArrayList<Adventure> adventureDeck = new ArrayList<>();//125
    public ArrayList<Adventure> discardAdventureDeck = new ArrayList<>();

    private ArrayList <Foe> tempFoeDeck = new ArrayList<>();
    private ArrayList <Weapon> tempWeaponDeck = new ArrayList<>();
    private ArrayList <Ally> tempAllyDeck = new ArrayList<>();
    private ArrayList <Amour> tempAmourDeck = new ArrayList<>();
    private ArrayList <Test> tempTestDeck = new ArrayList<>();

    public ArrayList<Story> storyDeck = new ArrayList<Story>();//28
    public ArrayList<Story> discardStoryDeck = new ArrayList<Story> ();

    public ArrayList<Player> playerList = new ArrayList<>();
    public int numPlayers = 0;
    public int playerTurn = 0;
    private int nextPlayerToAsk = 0;
    public boolean firstPass = true;
    public boolean gameOver = false;

    public int questBonusShields = 0; //For Kings Recognition TODO needs to be reset after first quest completed

    public GameBoard(SocketHandler controller){
        this.controller = controller; //modified the DI
        createDeck();
        sortSubDecks();
    }

    public int getNextPlayerToAsk(){
        return nextPlayerToAsk;
    }

    public void deal(){
        for (int y = 0; y < playerList.size(); y++) {
            for(int i = playerList.get(y).cardsInHand.size(); i < 12; i++) {
                playerList.get(y).cardsInHand.add(adventureDeck.get(adventureDeck.size() - 1));
                discardAdventureDeck.add(adventureDeck.get(adventureDeck.size() - 1));
                adventureDeck.remove(adventureDeck.size() - 1);
            }
        }
    }

    public Card draw(String whichDeck){
        if (storyDeck.size() == 0) {
            storyDeck = discardStoryDeck;
            shuffle ("storyDeck");
            discardStoryDeck = new ArrayList<>();
        }
        if (adventureDeck.size() == 0) {
            adventureDeck = discardAdventureDeck;
            shuffle("adventureDeck");
            discardAdventureDeck = new ArrayList<>();
        }
        switch (whichDeck) {
            case "Story":
                logIt.info("Draw deck has been called with this card: "+storyDeck.get(storyDeck.size() - 1).GetName ());
                discardStoryDeck.add (storyDeck.get(storyDeck.size() - 1));
                storyDeck.remove(storyDeck.size() - 1);
                return discardStoryDeck.get(discardStoryDeck.size() - 1);
            default://For Adventure card drawing
                logIt.info("Draw deck has been called with this card: "+adventureDeck.get(adventureDeck.size() - 1).GetName ());
                discardAdventureDeck.add (adventureDeck.get(adventureDeck.size() - 1)); //Not sure if good use for this yet
                adventureDeck.remove(adventureDeck.size() - 1);
                return discardAdventureDeck.get(discardAdventureDeck.size() - 1);
        }
    }

    public void shuffle(String whichDeck){
        Random generator = new Random(UUID.randomUUID().hashCode());

        switch (whichDeck) {
            case "storyDeck":
                for (int y = 0; y < 3; y++) {
                    for (int i = 0; i < storyDeck.size(); i++) {
                        //We should only ever shuffle a full story deck, for right now that's only at the start
                        //Will have to deal with situation: All story cards are used, but no one has won
                        int tempInt = generator.nextInt(storyDeck.size());
                        Story swapCard = storyDeck.get(tempInt);
                        storyDeck.set(tempInt, storyDeck.get(i));
                        storyDeck.set(i,swapCard);
                    }
                }
                break;
            default:
                for (int y = 0; y < 3; y++) {
                    for (int i = 0; i < adventureDeck.size(); i++) {
                        //Shuffling at the start is a full deck, but it could be a different number next time
                        int tempInt = generator.nextInt(adventureDeck.size());
                        Adventure swapCard = adventureDeck.get(tempInt);
                        adventureDeck.set(tempInt, adventureDeck.get(i));
                        adventureDeck.set(i, swapCard);
                    }
                }
                break;
        }
    }


    public String entryParser(String entry, String [] valuesToStrip){
        for(int i = 0; i < valuesToStrip.length; i++){
            entry = entry.replace(valuesToStrip[i], "");
        }
        return entry;
    }

    public ArrayList<Integer> setPlayerHand(int position, String entry){
        ArrayList<Integer> toReturn = new ArrayList<>();

        logIt.info("setPlayerHand "+position+ " , Entry is now: " + entry);
        String [] tokens = entry.split("[,]");
        for(int i = 0; i < tokens.length; i++){
            try {
                int parsedInt = Integer.parseInt(tokens[i]);
                playerList.get(position).cardsInHand.add(adventureDeck.get(parsedInt));
                toReturn.add(parsedInt);
            }catch(Exception e) {
                e.printStackTrace();
                logIt.error("Critical error in setPlayerHand, couldn't convert int");
            }
        }

    return toReturn;
    }

    public void removeSetCards(ArrayList<Integer> cardsToRemove){
        Collections.sort(cardsToRemove, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o2 - o1;
            }
        });
        System.out.println("Sorted cards in removeSetCards method:");
        for(int i = 0; i < cardsToRemove.size(); i++){
            logIt.debug(cardsToRemove.get(i));
            discardAdventureDeck.add(adventureDeck.remove(cardsToRemove.get(i).intValue()));
        }
        logIt.info("discardAdventureDeck size: " + discardAdventureDeck.size());
        logIt.info("adventureDeck size: " + adventureDeck.size());
    }

    public void removeFromPlayerHand(String indexes, int position, boolean calcNecessary){
        logIt.debug("We are in remove from player hand. Indexes is: " + indexes);
        ArrayList<Integer> needToSortIndexes = new ArrayList<>();
        String [] tokens = indexes.split("[,]");
        if(!tokens[0].equals("")) {
            for (int i = 0; i < tokens.length; i++) {
                try {
                    int parsedInt = Integer.parseInt(tokens[i]);
                    needToSortIndexes.add(parsedInt);
                } catch (Exception e) {
                    e.printStackTrace();
                    logIt.error("Critical error in setPlayerHand");
                }
            }
        }
        Collections.sort(needToSortIndexes, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o2 - o1;
            }
        });
        calculatePlayerBattleScore(position, needToSortIndexes, calcNecessary);

    }

    public void calculatePlayerBattleScore(int position, ArrayList<Integer> cardsSelected, boolean calcNecessary){

        Player p = playerList.get(position);

        if(!calcNecessary) {
            logIt.debug("entered this method");
            for(int i = 0; i < cardsSelected.size(); i++){
                logIt.debug(cardsSelected.get(i));
                int x = 0;
                try {
                    x = Integer.parseInt(cardsSelected.get(i) + "");
                }catch (Exception e){
                    e.printStackTrace();
                }
                logIt.debug(p.cardsInHand.remove(x));
            }
            logIt.debug("player cards in hand size is : "+p.cardsInHand.size());
            logIt.debug("card selected size is: "+cardsSelected.size());
            return;
        }
        boolean amourPlayer = false;
        boolean sirT = false, queenI = false;

        for(Ally a : p.alliesInPlay){
            p.roundBattleScore += a.GetBattlePoints();
            if(a.name.equals("Sir Tristan")){
                sirT = true;
            }else if(a.name.equals("Queen Iseult")){
                queenI = true;
            }
        }
        p.roundBattleScore += Player.rankDictionary.get(p.getRank());

        if(sirT && queenI) p.roundBattleScore += 10; //initial 10 already accounted for

        for(int i = cardsSelected.size() - 1; i > -1; i--){
            Adventure a = p.cardsInHand.get(cardsSelected.get(i));
            logIt.debug("Checking card " + a.GetName());
            if(a instanceof  Weapon){
                for(int y = i - 1; y > -1; y--){
                    Adventure cardToBeCompared = p.cardsInHand.get(cardsSelected.get(y));
                    if(p.cardsInHand.get(cardsSelected.get(i)) instanceof  Weapon && cardToBeCompared instanceof  Weapon && (cardToBeCompared.GetName().equals(p.cardsInHand.get(cardsSelected.get(i)).GetName()))) {
                        //we cant just use cardToBeRemoved because of modifications to i
                        logIt.debug("Cards being compared: " + p.cardsInHand.get(cardsSelected.get(i)) + " | " + cardToBeCompared);
                        int b = cardsSelected.remove(y);
                        logIt.debug("The card " + cardToBeCompared.GetName() + "was removed successfully. Index of " + b + ". Only one of each weapon type is allowed");
                        i = cardsSelected.size() - 1; //have to reset this now because a card was removed
                    }
                }
            }else if(a instanceof  Amour){
                if(!amourPlayer){
                    amourPlayer = true;
                    logIt.info("One amour allowed, card valid");
                }else if(amourPlayer){
                    cardsSelected.remove(i);
                    logIt.info("Only one amour allowed, card invalid");
                }
            }else if(a instanceof  Ally){
                logIt.info(" valid Ally card, dealt with in next loop");
            }else{
                cardsSelected.remove(i);
                logIt.info("Invalid card for tournament");
            }
        }

        for(int i = 0; i < cardsSelected.size(); i++){
            //TODO modify this to the above as well
            Adventure a = p.cardsInHand.get(cardsSelected.get(i));
            p.roundBattleScore += a.GetBattlePoints();
            logIt.debug(p.getName() + " played " + a.GetName());
            p.cardsInHand.remove(cardsSelected.get(i));
            if(a instanceof  Ally){
                p.alliesInPlay.add((Ally) a);
            }
        }
        logIt.debug("Number of allies in players hands now is " + p.alliesInPlay.size());
        logIt.debug(p.getName() + "battle score is now " + p.roundBattleScore);
    }


    //send arraylist of players who are winners using websocket session
    //send a message to client whether they won, lost or tie
    //in a tie ask a person again, call the satName());
    //            p.cardsInHand.remove(cardsSelected.get(i));
    //            if(a instanceof  Ally){
    //                p.alliesInPlay.add((Ally) a);
    //            }
    //        }
    //        logIt.debug("Number of allies in players hame methods
    //boolean tie situation or not, don't need to return winners if it is a tie, otherwise return winners
    public ArrayList<Player> getTournamentWinner(ArrayList<Player> playerp){
        ArrayList<Player> winners = new ArrayList<>();
        for(Player p : playerp){
            if(winners.size() == 0){
                winners.add(p);
                continue;
            }else if(p.roundBattleScore > winners.get(0).roundBattleScore){
                winners.clear();
                winners.add(p);
            }else if(p.roundBattleScore == winners.get(0).roundBattleScore){
                winners.add(p);
            }
        }
        logIt.info("No. of winners is: " + winners.size());
        resetPlayerBattleScore();
        return winners;
    }

    public void addShields(ArrayList<Player> winners, Story storyCard, int extraShields){
        if(storyCard instanceof Tournament){
            extraShields += ((Tournament) storyCard).shieldModifier;
        }else if(storyCard instanceof Quest){
            extraShields += ((Quest) storyCard).GetNumStages();
        }

        for(Player p : winners){
            p.setShields(p.getShields() + extraShields);
        }

        gameOver = rankUp();
        if(gameOver) System.out.println("Someone has WON deal with this later");

    }

    public boolean canSponsor(Player p){
        boolean twinkies = false;
        Quest q = null;
        if(discardStoryDeck.get(discardStoryDeck.size() - 1) instanceof  Quest){
            q = (Quest) discardStoryDeck.get(discardStoryDeck.size() - 1);
        }else{
            logIt.debug("CRITICAL ERROR 9001, previous card was not a quest.");
            return false;
        }
        //Player p = playerList.get(tempPlayerTurn - 1);
        int testCounter = 0, foeCounter = 0, numStages = q.GetNumStages();
        String linkedFoe = q.GetLinkedFoe();

        ArrayList<Pair<Foe,Integer>> foes = new ArrayList<> ();
        ArrayList<Weapon> weapons = new ArrayList<> ();

        for (int i = 0; i < p.cardsInHand.size(); i++) {
            if (p.cardsInHand.get(i) instanceof Foe) {
                if (p.cardsInHand.get(i).GetName () .equals( linkedFoe)) {
                    foes.add (new Pair<> ((Foe)p.cardsInHand.get(i), ((Foe)p.cardsInHand.get(i)).GetBoostedBattlePoints ()));
                    foeCounter++;
                } else {
                    foes.add (new Pair<> ((Foe)p.cardsInHand.get(i), ((Foe)p.cardsInHand.get(i)).GetBattlePoints ()));
                    foeCounter++;
                }
            } else if (p.cardsInHand.get(i) instanceof Weapon) {
                weapons.add ((Weapon)p.cardsInHand.get(i));
            } else if (p.cardsInHand.get(i) instanceof Test) {
                testCounter = 1;//can only use one test on a quest Dr.Suess
            }
        }

        try {
            ArrayList<Pair<Foe,Integer>> uniqueFoes = new ArrayList<>();
            for(int i = 0; i < foes.size(); i++){
                boolean isIn = false;
                for(int y = 0; y < uniqueFoes.size(); y++){
                    if(uniqueFoes.get(y).getValue() == foes.get(i).getValue())
                        isIn = true;
                }
                if(!isIn)
                    uniqueFoes.add(foes.get(i));
            }

            ArrayList<Weapon> uniqueWeapons = new ArrayList<>();
            for(int i = 0; i < weapons.size(); i++){
                boolean isIn = false;
                for(int y = 0; y < uniqueWeapons.size(); y++){
                    if(uniqueWeapons.get(y).battlePoints == weapons.get(i).battlePoints)
                        isIn = true;
                }
                if(!isIn)
                    uniqueWeapons.add(weapons.get(i));
            }

            for (int i = 0; i < uniqueFoes.size(); i++) { //FOR TESTING PURPOSES ONLY
                System.out.println (i + ": " + uniqueFoes.get(i).getKey().GetName () + "|" + uniqueFoes.get(i).getValue());
            }
            for (int i = 0; i < uniqueWeapons.size(); i++) { //FOR TESTING PURPOSES ONLY
                System.out.println (i + ": " + uniqueWeapons.get(i).GetName () + "|" + uniqueWeapons.get(i).GetBattlePoints ());
            }

            for(int i = 0; i < p.cardsInHand.size(); i++){
                System.out.println(p.cardsInHand.get(i).GetName());
            }
            //Console.ReadLine ();
            if (numStages <= uniqueFoes.size() + uniqueWeapons.size() + testCounter && numStages <= foeCounter + testCounter) {
                twinkies = true;
            }

            return twinkies;

        }catch (Exception e){
            System.out.println (e.toString());
            System.out.println ("Unique foes / unique weapons group by exception caught");
            return twinkies;
        }
    }

    public LinkedHashMap<ArrayList<Adventure>, Integer> setUpStages(ArrayList<Pair<Integer, Integer>> stageAndIndex, Quest questCard, Player sponsorForQuest){
        ArrayList<ArrayList<Adventure>> arrListKeys = new ArrayList<>();//stage number is one greater than index in arraylist
        LinkedHashMap<ArrayList<Adventure>, Integer> cardsPerStage = new LinkedHashMap<>();

        for(int i = 0; i < questCard.GetNumStages(); i++){
            arrListKeys.add(new ArrayList<Adventure>());
        }

        for(int i = 0; i < stageAndIndex.size(); i++){
            int stageNo = stageAndIndex.get(i).getKey();
            int cardIndex = stageAndIndex.get(i).getValue();
            arrListKeys.get(stageNo - 1).add(sponsorForQuest.cardsInHand.get(cardIndex));
        }

        for(int i = 1; i <= questCard.GetNumStages(); i++){//-1 will be updated if its a foe stage, otherwise it will be -1 to indicate test
            cardsPerStage.put(arrListKeys.get(i - 1), -9001);
        }

        ArrayList<String> weaponNames = new ArrayList<>();
        //ArrayList<Integer> savedBattlePoints = new ArrayList<>();
        boolean testUsed = false;
        int previousRoundBattleScore = 0;
        int currentBattleScore = 0;
        Set entrySet = cardsPerStage.entrySet();
        Iterator it = entrySet.iterator();
        while(it.hasNext()){
            Map.Entry<ArrayList<Adventure>, Integer> entry = (Map.Entry<ArrayList<Adventure>, Integer>) it.next();
            logIt.debug("Here are the cards: ");
            boolean hasFoeOrTest = false;
            boolean inValidCard = false;
            for(Adventure a : entry.getKey()){
                logIt.debug(a.GetName());
                if(a instanceof  Test) {
                    if(testUsed){
                        inValidCard = true;
                        break;
                    }else if(entry.getKey().size() > 1){
                        inValidCard = true;
                        break;
                    }
                    hasFoeOrTest = true;
                    testUsed = true;
                }else if(a instanceof Foe) {
                    if (!hasFoeOrTest) {
                        hasFoeOrTest = true;
                        if (a.name.equals(questCard.GetLinkedFoe())) {
                            currentBattleScore += ((Foe) a).GetBoostedBattlePoints();
                        } else {
                            currentBattleScore += a.battlePoints;
                        }
                    } else {
                        inValidCard = true;
                        break;
                    }
                }else if(a instanceof  Weapon){
                    if(weaponNames.contains(a.name)){
                        inValidCard = true;
                        break;
                    }else{
                        weaponNames.add(a.name);
                        currentBattleScore += a.battlePoints;
                    }
                }else if(!(a instanceof  Weapon)){
                    inValidCard = true;
                    break;
                }
            }//end inner for

            weaponNames.clear();
            logIt.info("Previous battle score & Current battle score " + previousRoundBattleScore + " | " + currentBattleScore);
            if(hasFoeOrTest && !inValidCard){
                logIt.info("(Might be)Valid stage");
                if(entry.getKey().get(0) instanceof  Test){
                    logIt.info("Valid test stage");
                    cardsPerStage.put(entry.getKey(), -1);
                    //should stay at -1 here
                }else{
                    if(currentBattleScore <= previousRoundBattleScore){
                        logIt.info("Invalid Stage, insufficient battle points for stage");
                        break;
                        //TODO add functionality here
                    }else{
                        logIt.info("Valid stage, battle score sufficient");
                        previousRoundBattleScore = currentBattleScore;
                        cardsPerStage.put(entry.getKey(), currentBattleScore);
                        currentBattleScore = 0;
                    }
                }
            }else{
                logIt.info("Invalid stage! Auto selecting cards");
                for(int i = 0; i < entry.getKey().size(); i++){
                    logIt.debug(entry.getKey().get(i));
                }
                break;
                //TODO add functionality here
            }
        }

        it = entrySet.iterator(); //iterator back to the start of set
        //Player host = playerList.get(playerTurn - 1);
        logIt.debug("Player name is (should be current host) " + sponsorForQuest.getName());
        while(it.hasNext()){
            Map.Entry<ArrayList<Adventure>, Integer> entry = (Map.Entry<ArrayList<Adventure>, Integer>) it.next();
            ArrayList<Adventure> adventureArrayList = entry.getKey();
            for(Adventure a : adventureArrayList){
                //testing remove by object instead of index
                sponsorForQuest.cardsInHand.remove(a);
            }
        }

        return cardsPerStage;
    }

    public void isEvent(Event storyCard) throws Exception{
        logIt.debug(storyCard.GetName() + ": " + storyCard.GetDescription());
        ArrayList<Player> playersTargeted = getPlayersTargeted(storyCard.GetWhosAffected());
        int tempPlayerturn = playerTurn;
        for (int i = 0; i < numPlayers; i++) { //add order here using tempPlayerTurn, playerTurn and checking if in list
            if (tempPlayerturn > numPlayers) {
                tempPlayerturn = 1;
            }

            Player successfulCandidate = null;
            for (int j = 0; j < playersTargeted.size(); j++) {
                if (playersTargeted.get(j) == playerList.get(tempPlayerturn - 1)) {
                    successfulCandidate = playersTargeted.get(j);
                    break;
                }
            }

            if (successfulCandidate != null) {
                logIt.info("Successful Candidate: " + successfulCandidate.getName());
                Player p = successfulCandidate;
                p.setShields(p.getShields() + storyCard.GetShieldModifier());
                if (p.getShields() < 0)
                    p.setShields(0);
                for (int y = 0; y < storyCard.GetAdventureCardModifier(); y++) {
                    p.cardsInHand.add((Adventure) draw("Adventure"));
                }
                if (storyCard.GetEliminateAllies()) {
                    p.alliesInPlay.clear();
                }
                if (storyCard.GetWeaponCardModifier() > 0) { //then we know its King's Call to Arms
                    logIt.info("Kings call to Arms  " + storyCard.name + " | " + storyCard.GetDescription());
                    //TODO send notification to everyone that player p was affected AND ask player to discard
                }else{
                    logIt.info("Any other event card " + storyCard.name + " | " + storyCard.GetDescription());
                    //TODO send notification here that player p was afftected by event card
                    for(Player player: playerList){
                        if(player == p){
                            controller.notify(p.getSession(), "success", "You were affected by " + storyCard.name);
                        } else {
                            controller.notify(p.getSession(), "info", p.getName() + " was affected by " + storyCard.name);
                        }

                    }

                }
                //kings recognition is handled strictly in getPlayersTargeted
                tempPlayerturn++;
            }
        }//end outer for
    }

    public ArrayList<Player> getPlayersTargeted(String whosAffected){ //this gets a list of whos affected, do something similar to temp player turn elsewhere to maintain discard order
        ArrayList<Player> p = new ArrayList<Player> ();
        if(whosAffected.equals(Event.playersTargeted.get(0))){//LowestRankAndShield
            for (int i = 0; i < playerList.size(); i++) {
                if (i == 0) {
                    p.add (playerList.get(i));
                }
                if (i != 0 && Player.rankDictionary.get(playerList.get(i).getRank()) < Player.rankDictionary.get(p.get(0).getRank())) {//even if there's more than one player, it means they are same shield and rank so we can always compare with 0
                    p.clear ();
                    p.add (playerList.get(i));
                }else if (i != 0 && playerList.get(i).getRank().equals(p.get(0).getRank())) {
                    if (playerList.get(i).getShields() < p.get(0).getShields()) {
                        p.clear ();
                        p.add (playerList.get(i));
                    } else if (playerList.get(i).getShields() == p.get(0).getShields()) {
                        p.add (playerList.get(i));
                    }
                }
            }
        }else if(whosAffected.equals(Event.playersTargeted.get(1))){//All
            for (int i = 0; i < playerList.size(); i++) {
                p.add (playerList.get(i));
            }
        }else if(whosAffected.equals(Event.playersTargeted.get(2))){//AllExceptDrawer
            int tempPlayerTurn = playerTurn;
            for (int i = 0; i < playerList.size(); i++) {//Accessing raw player turn causes problems and shouldn't be used for indexing
                if (tempPlayerTurn > numPlayers)
                    tempPlayerTurn = 1;
                if (i != (tempPlayerTurn - 1)) {
                    p.add (playerList.get(i));
                }
            }
        }else if(whosAffected.equals(Event.playersTargeted.get(3))){//DrawerOnly
            int tempPlayerTurn = playerTurn;
            if (tempPlayerTurn > numPlayers)//Accessing raw player turn causes problems and shouldn't be used for indexing
                tempPlayerTurn = 1;
            //Console.WriteLine (playerTurn - 1);
            p.add (playerList.get(tempPlayerTurn - 1));
        }else if(whosAffected.equals(Event.playersTargeted.get(4))){//HighestRanked
            for (int i = 0; i < playerList.size(); i++) {
                if (i == 0) {
                    p.add (playerList.get(i));
                }
                if (i != 0 && Player.rankDictionary.get(playerList.get(i).getRank()) > Player.rankDictionary.get(p.get(0).getRank())) {
                    p.clear ();
                    p.add (playerList.get(i));
                } else if (i != 0 && playerList.get(i).getRank().equals(p.get(0).getRank())) {
                    p.add (playerList.get(i));
                }
            }
        }else if(whosAffected.equals(Event.playersTargeted.get(5))){//LowestRanked
            for (int i = 0; i < playerList.size(); i++) {
                if (i == 0) {
                    p.add (playerList.get(i));
                }
                if (i != 0 && Player.rankDictionary.get(playerList.get(i).getRank()) < Player.rankDictionary.get(p.get(0).getRank())) {
                    p.clear ();
                    p.add (playerList.get(i));
                } else if (i != 0 && playerList.get(i).getRank().equals(p.get(0).getRank())) {
                    p.add (playerList.get(i));
                }
            }
        }else if(whosAffected.equals(Event.playersTargeted.get(6))){//Next
            questBonusShields += 2;
        }
        return p;
    }

    public void resetPlayerBattleScore(){
        for(Player p : playerList){
            p.roundBattleScore = 0;
        }
    }

    public int askNext(){//return -1 to know when to stop
        if(nextPlayerToAsk >= numPlayers) nextPlayerToAsk = 0;
        if(nextPlayerToAsk == playerTurn){
            if(firstPass){
                firstPass = false;
                nextPlayerToAsk++;
                logIt.debug(nextPlayerToAsk + " this is npta");
                return nextPlayerToAsk;
            }else{
                logIt.debug("First pass is false");
                return -1;
            }
        }else{
            nextPlayerToAsk++;
            logIt.debug(nextPlayerToAsk + " this is npta");
            return nextPlayerToAsk;
        }
    }

    public int whosTurn(){ //get at play.playerList(playerTurn - 1). Every a story card id drawn, call whosTurn
        logIt.debug("nextPlayerToAsk is "+nextPlayerToAsk);
        logIt.debug("player turn is "+playerTurn);
        if (playerTurn >= numPlayers) {
            System.out.println("player turn reset");
            playerTurn = 0;
        }
        System.out.println("Entered who's turn: " + playerTurn);
        playerTurn++;
        nextPlayerToAsk = playerTurn;
        logIt.debug("next player to ask | player turn " + nextPlayerToAsk  + "|" + playerTurn);
        firstPass = true;
        return playerTurn;
    }

    public boolean rankUp(){
        boolean hasWon = false;
        for (int i = 0; i < playerList.size(); i++) {
            Player p = playerList.get(i);
            if (p.getRank() == "Squire" && p.getShields() >= 5) {
                p.setRank("Knight");
                p.setShields(p.getShields() -5);
            }
            if (p.getRank() == "Knight" && p.getShields() >= 7) {
                p.setRank("Champion Knight");
                p.setShields(p.getShields() - 7);
            }
            if (p.getRank() == "Champion Knight" && p.getShields() >= 10) {
                //Player has won and become a knight of the round
                hasWon = true;
            }
        }
        return hasWon;
    }

    public void sortSubDecks(){
        Collections.sort(tempFoeDeck, new Comparator<Foe>() {
            @Override
            public int compare(Foe o1, Foe o2) {
                return o1.battlePoints - o2.battlePoints;
            }
        });
        Collections.sort(tempWeaponDeck, new Comparator<Weapon>() {
            @Override
            public int compare(Weapon o1, Weapon o2) {
                return o1.battlePoints - o2.battlePoints;
            }
        });
        Collections.sort(tempAllyDeck, new Comparator<Ally>() {
            @Override
            public int compare(Ally o1, Ally o2) {
                return o1.battlePoints - o2.battlePoints;
            }
        });
        //No need to sort Amours
        //No need to sort Tests
        adventureDeck.addAll(tempFoeDeck);
        adventureDeck.addAll(tempWeaponDeck);
        adventureDeck.addAll(tempAllyDeck);
        adventureDeck.addAll(tempAmourDeck);
        adventureDeck.addAll(tempTestDeck);
    }

    public void createDeck() {
        int cardCounter = 0;

        //ADVENTURE DECK

        /* AMOUR */

        for (int i = 0; i < 8; i++) {
            tempAmourDeck.add((Amour)createAdventureCard("Amour", "Amour", 10, 1, 0));
            cardCounter++;
        }

        /* WEAPON */

        for (int i = cardCounter; i < 10; i++) {
            tempWeaponDeck.add((Weapon)createAdventureCard("Weapon", "Excalibur", 30, 0, 0));
            cardCounter++;
        }

        for (int i = cardCounter; i < 16; i++) {
            tempWeaponDeck.add((Weapon)createAdventureCard("Weapon", "Lance", 20, 0, 0));
            cardCounter++;
        }

        for (int i = cardCounter; i < 24; i++) {
            tempWeaponDeck.add((Weapon)createAdventureCard("Weapon", "Battle-ax", 15, 0, 0));
            cardCounter++;
        }

        for (int i = cardCounter; i < 40; i++) {
            tempWeaponDeck.add((Weapon)createAdventureCard("Weapon", "Sword", 10, 0, 0));
            cardCounter++;
        }

        for (int i = cardCounter; i < 51; i++) {
            tempWeaponDeck.add((Weapon)createAdventureCard("Weapon", "Horse", 10, 0, 0));
            cardCounter++;
        }

        for (int i = cardCounter; i < 57; i++) {
            tempWeaponDeck.add((Weapon)createAdventureCard("Weapon", "Dagger", 5, 0, 0));
            cardCounter++;
        }

        /* FOE */
        for (int i = cardCounter; i < 58; i++) {
            tempFoeDeck.add((Foe)createAdventureCard("Foe", "Dragon", 50, 70, 0));
            cardCounter++;
        }

        for (int i = cardCounter; i < 60; i++) {
            tempFoeDeck.add((Foe)createAdventureCard("Foe", "Giant", 40, 40, 0));
            cardCounter++;
        }

        for (int i = cardCounter; i < 64; i++) {
            tempFoeDeck.add((Foe)createAdventureCard("Foe", "Mordred", 30, 30, 0));
            cardCounter++;
        }

        for (int i = cardCounter; i < 66; i++) {
            tempFoeDeck.add((Foe)createAdventureCard("Foe", "Green Knight", 25, 40, 0));
            cardCounter++;
        }

        for (int i = cardCounter; i < 69; i++) {
            tempFoeDeck.add((Foe)createAdventureCard("Foe", "Black Knight", 25, 35, 0));
            cardCounter++;
        }

        for (int i = cardCounter; i < 75; i++) {
            tempFoeDeck.add((Foe)createAdventureCard("Foe", "Evil Knight", 20, 30, 0));
            cardCounter++;
        }

        for (int i = cardCounter; i < 83; i++) {
            tempFoeDeck.add((Foe)createAdventureCard("Foe", "Saxon Knight", 15, 25, 0));
            cardCounter++;
        }

        for (int i = cardCounter; i < 90; i++) {
            tempFoeDeck.add((Foe)createAdventureCard("Foe", "Robber Knight", 15, 15, 0));
            cardCounter++;
        }

        for (int i = cardCounter; i < 95; i++) {
            tempFoeDeck.add((Foe)createAdventureCard("Foe", "Saxons", 10, 20, 0));
            cardCounter++;
        }

        for (int i = cardCounter; i < 99; i++) {
            tempFoeDeck.add((Foe)createAdventureCard("Foe", "Boar", 5, 15, 0));
            cardCounter++;
        }

        for (int i = cardCounter; i < 107; i++) {
            tempFoeDeck.add((Foe)createAdventureCard("Foe", "Thieves", 5, 5, 0));
            cardCounter++;
        }

        /*TEST*/

        tempTestDeck.add((Test)createAdventureCard("Test", "Test of Valor", 0, 1, 0));
        tempTestDeck.add((Test)createAdventureCard("Test", "Test of Valor", 0,1, 0));

        tempTestDeck.add((Test)createAdventureCard("Test", "Test of Temptation", 0,1, 0));
        tempTestDeck.add((Test)createAdventureCard("Test", "Test of Temptation", 0,1, 0));

        tempTestDeck.add((Test)createAdventureCard("Test", "Test of the Questing Beast", 0, 4, 0));
        tempTestDeck.add((Test)createAdventureCard("Test", "Test of the Questing Beast", 0, 4, 0));

        tempTestDeck.add((Test)createAdventureCard("Test", "Test of Morgan Le Fey", 0, 3, 0));
        tempTestDeck.add((Test)createAdventureCard("Test", "Test of Morgan Le Fey", 0, 3, 0));


        /*Allies*/
        tempAllyDeck.add((Ally)createAdventureCard("Ally", "Sir Galahad", 15, 0, 15));
        tempAllyDeck.add((Ally)createAdventureCard("Ally", "Sir Gawain", 10, 0, 20));
        tempAllyDeck.add((Ally)createAdventureCard("Ally", "King Pellinore", 10, 0, 10));
        tempAllyDeck.add((Ally)createAdventureCard("Ally", "Sir Percival", 5, 0, 20));
        tempAllyDeck.add((Ally)createAdventureCard("Ally", "Sir Tristan", 10, 0, 20));
        tempAllyDeck.add((Ally)createAdventureCard("Ally", "King Arthur", 10, 2, 10));
        tempAllyDeck.add((Ally)createAdventureCard("Ally", "Queen Guinevere", 0, 3, 0));
        tempAllyDeck.add((Ally)createAdventureCard("Ally", "Merlin", 0, 0, 0));
        tempAllyDeck.add((Ally)createAdventureCard("Ally", "Queen Iseult", 0, 2, 0));
        tempAllyDeck.add((Ally)createAdventureCard("Ally", "Sir Lancelot", 15, 0, 25));


        //STORY DECK
        cardCounter = 0;

        /* Tournament */
        /*EVENT*/
        storyDeck.add(createEventCard("Chivalrous Deed", 3, 0, false, 0, 0, Event.playersTargeted.get(0), "Player(s) with both lowest rank and least amount of shields, receives 3 shields"));
        storyDeck.add(createEventCard("Pox", -1, 0, false, 0, 0, Event.playersTargeted.get(2), "All players except the player drawing this card lose 1 shield"));
        storyDeck.add(createEventCard("Plague", -2, 0, false, 0, 0, Event.playersTargeted.get(3), "Drawer loses two shields if possible"));
        storyDeck.add(createEventCard("King's Recognition", 2, 0, false, 0, 0, Event.playersTargeted.get(6), "The next player(s) to complete a Quest will receive 2 extra shields"));
        storyDeck.add(createEventCard("King's Recognition", 2, 0, false, 0, 0, Event.playersTargeted.get(6), "The next player(s) to complete a Quest will receive 2 extra shields"));
        storyDeck.add(createEventCard("Queen's Favor", 0, 2, false, 0, 0, Event.playersTargeted.get(5), "The lowest ranked player(s) immediately receives 2 Adventure Cards"));
        storyDeck.add(createEventCard("Queen's Favor", 0, 2, false, 0, 0, Event.playersTargeted.get(5), "The lowest ranked player(s) immediately receives 2 Adventure Cards"));
        storyDeck.add(createEventCard("Court Called to Camelot", 0, 0, true, 0, 0, Event.playersTargeted.get(1), "All Allies in play must be discarded"));
        storyDeck.add(createEventCard("Court Called to Camelot", 0, 0, true, 0, 0, Event.playersTargeted.get(1), "All Allies in play must be discarded"));
        //storyDeck.add(createEventCard("King's Call To Arms", 0, 0, false, 1, 2, Event.playersTargeted.get(4), "The highest ranked player(s) must place 1 weapon in the discard pile. If unable to do so, 2 Foe Cards must be discarded"));
        storyDeck.add(createEventCard("Prosperity Throughout the Realm", 0, 2, false, 0, 0, Event.playersTargeted.get(1), "All players may immeadiately draw two Adventure Cards"));

        /*Tournament*/
//        storyDeck.add(createTournamentCard("AT YORK", 0));
//        storyDeck.add(createTournamentCard("AT TINTAGEL", 1));
//        storyDeck.add(createTournamentCard("AT ORKNEY", 2));
//        storyDeck.add(createTournamentCard("AT CAMELOT", 3));

        /*Quest*/
//        storyDeck.add(createQuestCard("Journey through the Enchanted Forest", 3, "Evil Knight", "none"));
//        storyDeck.add(createQuestCard("Vanquish King Arthur's Enemies", 3, "none", "none"));
//        storyDeck.add(createQuestCard("Vanquish King Arthur's Enemies", 3, "none", "none"));
//        storyDeck.add(createQuestCard("Repel the Saxon Raiders", 2, "All Saxons", "none"));
//        storyDeck.add(createQuestCard("Repel the Saxon Raiders", 2, "All Saxons", "none"));
//        storyDeck.add(createQuestCard("Boar Hunt", 2, "Boar", "none"));
//        storyDeck.add(createQuestCard("Boar Hunt", 2, "Boar", "none"));
//        storyDeck.add(createQuestCard("Search for the Questing Beast", 4, "none", "King Pellinore"));
//        storyDeck.add(createQuestCard("Defend the Queens Honor", 4, "All", "Sir Lancelot"));
//        storyDeck.add(createQuestCard("Slay the Dragon", 3, "Dragon", "none"));
//        storyDeck.add(createQuestCard("Rescue the Fair Maiden", 3, "Black Knight", "none"));
//        storyDeck.add(createQuestCard("Search for the Holy Grail", 5, "All", "Sir Percival"));
//        storyDeck.add(createQuestCard("Test of the Green Knight", 4, "Green Knight", "Sir Gawain"));


    }

    public Story createEventCard(String name, int shieldModifier, int adventureCardModifier, boolean eliminiateAllies, int weaponCardModifier,
                                 int foeCardModifier, String whosAffected, String description){
        Event eve = new Event(name, shieldModifier, adventureCardModifier, eliminiateAllies, weaponCardModifier, foeCardModifier, whosAffected, description);
        return eve;
    }

    public Story createTournamentCard(String name, int shieldModifier){
        Tournament tournament = new Tournament(name, shieldModifier);
        return tournament;
    }

    public Story createQuestCard(String name, int numStages, String linkedFoe, String linkedAlly){
        Quest quest = new Quest(name, numStages, linkedFoe, linkedAlly);
        return quest;
    }

    public Adventure createAdventureCard(String type, String name, int battlePoints, int bids, int boostedBattlePoints) {
        switch (type) {
            case "Foe":
                Foe foe = new Foe(name, battlePoints, bids);//using bids as boosted battle points here..not good practice?
                return foe;
            case "Weapon":
                Weapon weapon = new Weapon(name, battlePoints);
                return weapon;
            case "Ally":
                Ally ally = new Ally(name, battlePoints, bids, boostedBattlePoints);
                return ally;
            case "Amour":
                Amour amour = new Amour();
                return amour;
            case "Test":
                Test test = new Test(name, bids);
                return test;
            default:
                System.out.println("Deck creation err, abort");
                Foe apocalypse = new Foe("Critical Err", 9001, -1);
                return apocalypse;
        }
    }

}
