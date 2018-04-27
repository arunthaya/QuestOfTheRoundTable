package com.devglan.config;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
//import javafx.util.Pair;
import model.*;
//import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
//import org.json.simple.JSONArray;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.devglan.config.Application.logIt;
import static java.lang.Integer.parseInt;

@Component
public class SocketHandler extends TextWebSocketHandler {
	
	List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
	List<WebSocketSession> gameRoomSessions = new ArrayList<WebSocketSession>();
	List<String> users = new ArrayList<String>();
	List<String> aiUsers = new ArrayList<>();
	ArrayList<Integer> orderOfPlayersToAskQuest = new ArrayList<>();
	ArrayList<Player> playersParticipating = new ArrayList<Player>();
	LinkedList<WebSocketSession> playerBids = new LinkedList<>();
	LinkedList<WebSocketSession> orderOfPlayers = new LinkedList<>();
	Player sponsorForQuest;
	String[] aiNames = {"AIPlayer1","AIPlayer2","AIPlayer3","AIPlayer4"};
	private LinkedHashMap<ArrayList<Adventure>, Integer> cardsAndBattlePoints;
	private WebSocketSession host;
	private int AI_PLAYERS = 0;
	private int MAX_HUMANPLAYERS = 0;
	private int TOTAL_PLAYERS = 0;
	private int CURRENTPLAYERS = 0;
	private int PLAYER_READYCOUNT = 0;
	private int STORY_PLAYERCOUNT = 0;
	private int FUNCTIONINVOKEDCOUNTER = 0;
	private int askedPlayersToParticipateInQuest = 0;
	private int askedPlayersToSponsorQuest = 0;
	private int currentStageNow;
	private int playerBid = 0;
	private int stripCardsCounter = 0;
	private int mustStripCards = 0;
	private boolean IS_TIE = false;
	private boolean FIRSTPASS_BID = false;
	private boolean AI_TURNEDON = false;

	private boolean RACING_ENABLED = false;
	private boolean playersReadyRanOnce = false;
	public Story currentStory;
	public GameBoard play;

	public static final String METHOD_KEY = "method";
	public static final String BODY_KEY = "body";
	public static final String NUM_PLAYERS = "numPlayers";
	public static final String SCOREBOARD_INFO = "scoreboard";
	public static final String NAME = "name";
	public static final String NUM_STAGES = "numStages";

	public SocketHandler(){
		//System.out.println("Constructor for socket handler called");
		logIt.info("Setting up connection");
		play = new GameBoard(this);
	}


	@Override
	public void handleTextMessage(WebSocketSession session, TextMessage message)
			throws Exception {
		logIt.info("Incoming message from " + session.getId());
		Map<String, String> value = new Gson().fromJson(message.getPayload(), Map.class);
		String methodName = value.get("method");
		switch(methodName){
			case "newPlayer":
				logIt.info(" newPlayer method called");
				addNewPlayer(session, value.get("body"));
				break;
			case "alertUser" :
				logIt.info("alertUser method called");
				alertingUser(session, value.get("body"));
				break;
			case "createGame" :
				logIt.info("createGame method called");
				createGame(session, value.get("body"));
				break;
			case "joinGameRoom":
				logIt.info("Incoming message from " + session.getId());
				joinGame(session, value.get("body"));
				break;
			case "startGameRequested":
				logIt.info("Incoming message from " + session.getId());
				startGameRequested(session);
				break;
			case "hostSetUpGame":
				logIt.info("Incoming message from " + session.getId());
				startGame(session, value);
				break;
			case "addCards":
				logIt.info("Incoming message from " + session.getId());
				addCards(session);
				break;
			case "playerReady":
				logIt.info("Incoming message from " + session.getId());
				playerReady(session);
				//updateAlliesInPlay();
				break;
			case "hostForQuest":
				logIt.info("Incoming message from "+ session.getId());
				hostForQuest(session, value.get("body"));
				break;
			case "selectedCards": //for tourney
				logIt.info("Incoming message from " + session.getId());
				selectedCardsToBeRemovedFromCardsInHand(session, value);
				break;
			case "cardsForQuest":
				logIt.info("Incoming message from " + session.getId());
				cardsForQuest(session, value);
				break;
            case "playerParticipatingInQuest":
                logIt.info("Incoming message from "+ session.getId());
                playersParticipatingInQuest(session, value.get("body"));
                break;
            case "selectedCardsForQuest": //for quest
                logIt.info("Incoming message from " + session.getId());
                cardsUsedForQuest(session, value.get("body"), value.get("currentStage"), false);
                break;
			case "biddingRequest":
				logIt.info("Incoming message from " + session.getId());
				bidHandler(session, value, false, Integer.parseInt(value.get("currentStage")));
				break;
			case "biddingRemoveCards":
				logIt.info("Incoming message from " + session.getId());
				bidHandler(session, value, true, Integer.parseInt(value.get("currentStage")));
				break;
			case "removeExcessCards":
				logIt.info("incoming message from " + session.getId());
				stripCards(session, value);
				break;
			default:
				logIt.error("critical error 9001");
		}
	}
	
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		logIt.info("connection established with : "+ session.getId());
		sessions.add(session);
	}

	public void alertingUser(WebSocketSession session, String message) throws IOException{
		JsonObject response = new JsonObject();
		response.addProperty(METHOD_KEY, "alertingUser");
		response.addProperty(BODY_KEY, ".");
		session.sendMessage(new TextMessage(response.toString()));
	}

	public void addNewPlayer(WebSocketSession session, String message) throws IOException{
		logIt.info("Session : "+session.getId()+" is being added to the lobby, name is : "+message);
		for(WebSocketSession webSocketSession : sessions) {
			JsonObject response = new JsonObject();
			response.addProperty(METHOD_KEY, "greeting");
			response.addProperty(BODY_KEY, message + " is online!");
			webSocketSession.sendMessage(new TextMessage(response.toString()));
			if(webSocketSession != session){
				notify(webSocketSession, "info", ""+ message + " is now Online!");
			}
		}
	}

	public void createGame(WebSocketSession session, String body) throws IOException{
		this.host = session;
		String hostname = "";
		//logIt.info("Host session is "+session.getId()+ " ,");
		gameRoomSessions.add(session);
        Map<String, String> value = new Gson().fromJson(body, Map.class);
        CURRENTPLAYERS++;
        JsonArray bodyOfResponse = new JsonArray();
        for (Map.Entry<String, String> entry : value.entrySet())
        {
            //System.out.println(entry.getKey() + "/" + entry.getValue());
            if(entry.getKey().equals("humanPlayers")){
            	MAX_HUMANPLAYERS = parseInt(entry.getValue());
            	TOTAL_PLAYERS = MAX_HUMANPLAYERS;
			}
			if(entry.getKey().equals("host")){
            	users.add(entry.getValue());
            	logIt.info("Host is : " +entry.getValue()+ " ,session is "+session.getId());
            	hostname = entry.getValue();
			}

			if(entry.getKey().equals("aiPlayers")){
            	logIt.debug("aiPLayers is : "+entry.getValue());
            	if(Integer.parseInt(entry.getValue()) != 0) {
					AI_PLAYERS = Integer.parseInt(entry.getValue());
					AI_TURNEDON = true;
				}
			}

			//logIt.info("users size is "+users.size());
            JsonObject element = new JsonObject();
            element.addProperty(entry.getKey(), entry.getValue());
            bodyOfResponse.add(element);
        }

		JsonObject response = new JsonObject();
		for(WebSocketSession webSocketSession: sessions){
		    if(session == webSocketSession){
		    	response.addProperty(METHOD_KEY, "gameCreatedSuccessfully");
		        response.addProperty(BODY_KEY, "success");
		        response.addProperty(NUM_PLAYERS, CURRENTPLAYERS);
		        webSocketSession.sendMessage(new TextMessage(response.toString()));
            } else {
		    	response.addProperty(METHOD_KEY, "gameCreated");
				response.add(BODY_KEY, bodyOfResponse);
				response.addProperty(NUM_PLAYERS, 1);
                webSocketSession.sendMessage(new TextMessage(response.toString()));
                notify(webSocketSession, "success", ""+hostname+" created the game");
            }
        }
	}

	public void joinGame(WebSocketSession session, String body) throws IOException{
		CURRENTPLAYERS++;
		JsonObject response = new JsonObject();
		if(CURRENTPLAYERS > MAX_HUMANPLAYERS){
			response.addProperty(METHOD_KEY, "noGame");
			session.sendMessage(new TextMessage(response.toString()));
		} else {
			for (WebSocketSession webSocketSession : sessions) {
				if (webSocketSession == host) {
					response.addProperty(METHOD_KEY, "playerJoined");
					response.addProperty(BODY_KEY, body + " has joined.");
					response.addProperty(NUM_PLAYERS, CURRENTPLAYERS);
					webSocketSession.sendMessage(new TextMessage(response.toString()));
				}
				notify(webSocketSession, "success", ""+body+" just joined the gameroom");
			}
			session.sendMessage(new TextMessage(response.toString()));
			gameRoomSessions.add(session);
			users.add(body);
		}
	}

	public void startGameRequested(WebSocketSession session) throws IOException {
		logIt.info(" start game requested by host session : "+session.getId());
		for(int i=0; i<AI_PLAYERS; i++){
			users.add(aiNames[i]);
		}
		for(WebSocketSession webSocketSession : gameRoomSessions){
			if(webSocketSession == session){
				JsonObject response = new JsonObject();
				response.addProperty(METHOD_KEY, "startTheGame");
				response.addProperty(BODY_KEY, "host");
				for(int i=0; i<users.size(); i++){
					if(i==0){
						response.addProperty("playera", users.get(i));
					}
					if(i==1){
						response.addProperty("playerb", users.get(i));
					}
					if(i==2){
						response.addProperty("playerc", users.get(i));
					}
					if(i==3){
						response.addProperty("playerd", users.get(i));
					}
					if(users.get(i).contains("AI")){
						play.playerList.add(new AIPlayer(aiNames[i], "Squire", 0, "strategy2", play, i));
						PLAYER_READYCOUNT++;
					}else {
						play.playerList.add(new Player(users.get(i), "Squire", 0));
					}
					play.numPlayers++;
				}
				//ogIt.info("made it here");
				webSocketSession.sendMessage(new TextMessage(response.toString()));
			} else {
				//logIt.info("made it here");
				JsonObject response = new JsonObject();
				response.addProperty(METHOD_KEY, "startTheGame");
				response.addProperty(BODY_KEY, "hostStartedGame");
				webSocketSession.sendMessage(new TextMessage(response.toString()));
			}
		}
	}

	public void startGame(WebSocketSession session, Map<String, String> message) throws IOException {
		ArrayList<Integer> indexValues = new ArrayList<>();
		System.out.println("startgame key value iterator :");
		String [] tempArr = {"\"", "[", "]"};
		for (Map.Entry<String, String> entry : message.entrySet()){
			if(entry.getKey().equals("p1Cards")){
				String parsedEntry = play.entryParser(entry.getValue(), tempArr);
				indexValues.addAll(play.setPlayerHand(0, parsedEntry));
			}else if(entry.getKey().equals("p2Cards")){
				String parsedEntry = play.entryParser(entry.getValue(), tempArr);
				indexValues.addAll(play.setPlayerHand(1, parsedEntry));
			}else if(entry.getKey().equals("p3Cards")){
				String parsedEntry = play.entryParser(entry.getValue(), tempArr);
				indexValues.addAll(play.setPlayerHand(2, parsedEntry));
			}else if(entry.getKey().equals("p4Cards")){
				String parsedEntry = play.entryParser(entry.getValue(), tempArr);
				indexValues.addAll(play.setPlayerHand(3, parsedEntry));
			}
		}

		play.removeSetCards(indexValues);

		for (Map.Entry<String, String> entry : message.entrySet()){
			if(entry.getKey().equals("p1Shields")){
				play.playerList.get(0).setShields(parseInt(entry.getValue()));
			}
			else if(entry.getKey().equals("p2Shields")){
				play.playerList.get(1).setShields(parseInt(entry.getValue()));
			}
			else if(entry.getKey().equals("p3Shields")){
				play.playerList.get(2).setShields(parseInt(entry.getValue()));
			}
			else if(entry.getKey().equals("p4Shields")){
				play.playerList.get(3).setShields(parseInt(entry.getValue()));
			}

			if(entry.getKey().equals("p1Squire") && entry.getValue().equals("true")){
				play.playerList.get(0).setRank("Squire");
			}else if(entry.getKey().equals("p1Knight") && entry.getValue().equals("true")){
				play.playerList.get(0).setRank("Knight");
			}else if(entry.getKey().equals("p1CKnight") && entry.getValue().equals("true")){
				play.playerList.get(0).setRank("Champion Knight");
			}

			if(entry.getKey().equals("p2Squire") && entry.getValue().equals("true")){
				play.playerList.get(1).setRank("Squire");
			}else if(entry.getKey().equals("p2Knight") && entry.getValue().equals("true")){
				play.playerList.get(1).setRank("Knight");
			}else if(entry.getKey().equals("p2CKnight") && entry.getValue().equals("true")){
				play.playerList.get(1).setRank("Champion Knight");
			}

			if(entry.getKey().equals("p3Squire") && entry.getValue().equals("true")){
				play.playerList.get(2).setRank("Squire");
			}else if(entry.getKey().equals("p3Knight") && entry.getValue().equals("true")){
				play.playerList.get(2).setRank("Knight");
			}else if(entry.getKey().equals("p3CKnight") && entry.getValue().equals("true")){
				play.playerList.get(2).setRank("Champion Knight");
			}

			if(entry.getKey().equals("p4Squire") && entry.getValue().equals("true")){
				play.playerList.get(3).setRank("Squire");
			}else if(entry.getKey().equals("p4Knight") && entry.getValue().equals("true")){
				play.playerList.get(3).setRank("Knight");
			}else if(entry.getKey().equals("p4CKnight") && entry.getValue().equals("true")){
				play.playerList.get(3).setRank("Champion Knight");
			}

		}

		play.shuffle("Adventure");
		play.deal();

		JsonArray scoreBoard = new JsonArray();
		for(Player p : play.playerList){
			logIt.info(p.getName() + " | " + p.getShields() + " | " + p.getRank() + "|");
			for(Adventure card : p.cardsInHand) {
				logIt.info("card " + card.GetName());
			}
			logIt.info("^ player : "+p.getName() + " cards");
			JsonObject individualPlayer = new JsonObject();
			individualPlayer.addProperty("playerName", p.getName());
			individualPlayer.addProperty("playerCards", p.cardsInHand.size());
			individualPlayer.addProperty("playerShields", p.getShields());
			individualPlayer.addProperty("playerRank", p.getRank());
			scoreBoard.add(individualPlayer);
		}

		//logIt.debug("orderOfPlayers is: "+orderOfPlayers.size());
		for(int i=0; i<play.playerList.size(); i++){
			if(play.playerList.get(i) instanceof AIPlayer){
				logIt.debug("user is "+play.playerList.get(i).toString());
				for (Adventure card : play.playerList.get(i).cardsInHand) {
					logIt.debug("	card" + card.GetName());
				}
			} else {
				play.playerList.get(i).setSession(gameRoomSessions.get(i));
				orderOfPlayers.offer(gameRoomSessions.get(i));
				//logIt.debug("orderOfPlayers is: " + orderOfPlayers.size());
				JsonObject a = new JsonObject();
				a.addProperty(METHOD_KEY, "startedTheActualGame");

				JsonArray cardsInHandForPlayer = new JsonArray();
				for (Adventure card : play.playerList.get(i).cardsInHand) {
					JsonObject individualCard = new JsonObject();
					individualCard.addProperty("name", card.GetName());
					individualCard.addProperty("battlepoints", card.GetBattlePoints());
					cardsInHandForPlayer.add(individualCard);
				}
				a.add(BODY_KEY, cardsInHandForPlayer);
				a.add(SCOREBOARD_INFO, scoreBoard);
				logIt.info("cardsInHandForPlayer " + cardsInHandForPlayer.toString());
				play.playerList.get(i).getSession().sendMessage(new TextMessage(a.toString()));
			}
		}



	}

	public void addCards(WebSocketSession session){
		String cardsToRig = new Gson().toJson(play.adventureDeck); //am i really using this if not remove it
		JsonObject responseObj = new JsonObject();
		responseObj.addProperty(METHOD_KEY, "addRigCards");

		JsonArray bodyOfResponse = new JsonArray();
		for(int i=0; i<play.adventureDeck.size(); i++){
			JsonObject card = new JsonObject();
			card.addProperty("name", play.adventureDeck.get(i).GetName());
			bodyOfResponse.add(card);
		}
		responseObj.add(BODY_KEY, bodyOfResponse);
		try {
			session.sendMessage(new TextMessage(responseObj.toString()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public void selectedCardsToBeRemovedFromCardsInHand(WebSocketSession session, Map<String, String> message) throws IOException {
		logIt.info("Session: "+session.getId()+" , selectedCardsToBeRemovedFromCardsInHand");
		String indexes = "";
		for (Map.Entry<String, String> entry : message.entrySet()) {
			//logIt.info("Printing out incoming message from session");
			logIt.info("key " + entry.getKey() + " ,value:" + entry.getValue());
			if(entry.getKey().equals("body")){
				logIt.info("Reached entry that is the body from JSON");
				if(entry.getValue().equals("no")){
					TOTAL_PLAYERS--;
					logIt.info("Total_players is : "+TOTAL_PLAYERS);
				} else {
					logIt.info("Session "+session.getId() + " ,is entering tournament");
					indexes = entry.getValue();
					STORY_PLAYERCOUNT++;
					logIt.info("Story player count is "+STORY_PLAYERCOUNT);
					if(indexes.equals("")) logIt.error("CRITICAL ERROR 9001 in selectedCardsToBeRemovedFromCardsInHand");
					String [] tempArr = {"[", "]"};
					indexes = play.entryParser(indexes, tempArr);
					logIt.debug("indexes from tourney is: "+indexes);
					for(int i = 0; i < play.playerList.size(); i++) {
						if(play.playerList.get(i).getSession() == session) {
							play.removeFromPlayerHand(indexes, i, true);
							playersParticipating.add(play.playerList.get(i));
						}
					}
				}
			}
		}
		JsonObject response = new JsonObject();
		response.addProperty(METHOD_KEY, "tournamentWinner");
		if(STORY_PLAYERCOUNT == TOTAL_PLAYERS){
			logIt.info("All users have specified if they will participate or not in tournament");
			ArrayList<Player> winnerReturn = new ArrayList<>();
			winnerReturn = play.getTournamentWinner(playersParticipating);
			logIt.info("winner return size is : "+winnerReturn.size());
			if(winnerReturn.size() == 1){
				logIt.info("only winner is "+winnerReturn.get(0).getName());
				ArrayList<Story> t = play.discardStoryDeck;
				Story card = t.get(t.size()- 1);
				Tournament card1 = (Tournament) card;
				int shieldModifier = card1.GetShieldModifier();
				logIt.info("Current card is : " +card1.toString() + " ,shield modifier :" +shieldModifier);
				play.addShields(winnerReturn, card, shieldModifier);
				for(Player p: play.playerList){
					if(p.equals(winnerReturn.get(0))){
						logIt.info("Player name of winner is : "+p.getName());
						response.addProperty(BODY_KEY,"congratulations you've won");
						p.getSession().sendMessage(new TextMessage(response.toString()));
					} else {
						response.addProperty(BODY_KEY, winnerReturn.get(0).getName()+" has won");
						p.getSession().sendMessage(new TextMessage(response.toString()));
					}
				}
				try {
					playerReady(session);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			else if(winnerReturn.size() > 1){
				if(IS_TIE){
					String playerWinnerNames ="";
					System.out.println("There has been a tie again, all players involved in the tie will be awarded sheilds");
					for(Player p: winnerReturn){
						playerWinnerNames += p.getName() + " ";
					}
					for(Player p: play.playerList){
						JsonArray bodyOfResponse = new JsonArray();
						for(int i=0; i<p.cardsInHand.size(); i++){
							logIt.info("Player "+ p.getName() + " , cardsInHand size is: "+p.cardsInHand.size());
							JsonObject card = new JsonObject();
							card.addProperty("name", p.cardsInHand.get(i).GetName());
							card.addProperty("battlePoints", p.cardsInHand.get(i).GetBattlePoints());
							bodyOfResponse.add(card);
						}
						//logIt.info("JSON card array object is "+bodyOfResponse);
						response.add(BODY_KEY, bodyOfResponse);
						boolean isWinner = false;
						for(Player tiedPlayer: winnerReturn){
							if(tiedPlayer.equals(p)){
								isWinner = true;
								response.addProperty("stillInTournament", "no");
								response.addProperty("winner", "yes");
								p.getSession().sendMessage(new TextMessage(response.toString()));
							}
						}
						if(!isWinner){
							response.addProperty("stillInTournament", "no");
							response.addProperty("winner", "no");
							p.getSession().sendMessage(new TextMessage(response.toString()));
							logIt.debug("not a winner");
						}
					}
					IS_TIE = false;
					try {
						playerReady(session);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				else if(!IS_TIE){
					IS_TIE = true;
					String playerWinnerNames ="";
					for(Player p: winnerReturn){
						playerWinnerNames += p.getName() + " ";
					}
					response.addProperty("playersStillInTournament", playerWinnerNames);
					for(Player p: play.playerList){
						JsonArray bodyOfResponse = new JsonArray();
						for(int i=0; i<p.cardsInHand.size(); i++){
							JsonObject card = new JsonObject();
							card.addProperty("name", p.cardsInHand.get(i).GetName());
							card.addProperty("battlePoints", p.cardsInHand.get(i).GetBattlePoints());
							bodyOfResponse.add(card);
						}
						//logIt.info("JSON card array object is "+bodyOfResponse);
						response.add(BODY_KEY, bodyOfResponse);
						boolean isWinner = false;
						for(Player tiedPlayer: winnerReturn){
							if(tiedPlayer.equals(p)){
								isWinner = true;
								response.addProperty("stillInTournament", "yes");
								p.getSession().sendMessage(new TextMessage(response.toString()));
							}
						}
						if(!isWinner){
							response.addProperty("stillInTournament", "no");
							p.getSession().sendMessage(new TextMessage(response.toString()));
						}
					}
					STORY_PLAYERCOUNT = 0;
					TOTAL_PLAYERS = winnerReturn.size();
				}
			}else{
				logIt.error("Critical Error 9001. in checking winners in selectedCardsToBeRemovedFromCardsInHand");
			}

		}
	}

	public void playerReady(WebSocketSession session) throws Exception { //Drawing story card

        if(!playersReadyRanOnce) {
            PLAYER_READYCOUNT++;
        }
		System.out.println("playerReadyCount " + PLAYER_READYCOUNT);
		System.out.println("CurrentPlayers " + CURRENTPLAYERS);
		System.out.println("player ready called ");
//		if(playersReadyRanOnce){
//			mustStripCards = stripCardsNotifier();
//		}
		if(PLAYER_READYCOUNT == CURRENTPLAYERS) {
			stripCardsCounter = 0;
			mustStripCards = 0;
			playersReadyRanOnce = true;
			JsonObject response = new JsonObject();
			if(play.gameOver){
				//TODO send a message that games over
				logIt.info("Game over");
				return;
			}
			Story storyCard = (Story) play.draw("Story");
			logIt.info("storycard drawn is : " + storyCard.GetName());
			play.whosTurn();
			WebSocketSession temp = orderOfPlayers.pop();
			orderOfPlayers.offer(temp);
			askedPlayersToSponsorQuest = 0;
			String host = "";
			for(Player p:play.playerList){
				if(p.getSession() == temp){
					host = p.getName();
					notify(p.getSession(), "success", "You have drawn "+storyCard.GetName());
				} else {
					notify(p.getSession(), "info", host + " has drawn "+storyCard.GetName());
				}
			}
			if (storyCard instanceof Tournament) {
				response.addProperty(METHOD_KEY, "tournamentStarted");
				response.addProperty(BODY_KEY, storyCard.GetName());
				for (WebSocketSession webSocketSession : gameRoomSessions) {
					webSocketSession.sendMessage(new TextMessage(response.toString()));
				}
			} else if (storyCard instanceof Quest) {
				currentStory = storyCard;
				response.addProperty(METHOD_KEY, "questStarted");
				response.addProperty(BODY_KEY, storyCard.GetName());
				response.addProperty(NUM_STAGES, ((Quest) storyCard).GetNumStages());
				//logIt.debug("this is the player i am sending the message to rn " + play.playerList.get(play.playerTurn - 1).getName());
				temp.sendMessage(new TextMessage(response.toString()));
				//play.playerList.get(play.playerTurn - 1).getSession().sendMessage(new TextMessage(response.toString()));
				askedPlayersToSponsorQuest++;
			}else{
				for(Player p:play.playerList){
					JsonObject eventResponse = new JsonObject();
					eventResponse.addProperty(METHOD_KEY, "events");
					eventResponse.addProperty(BODY_KEY, ((Event) storyCard).GetName());
					p.getSession().sendMessage(new TextMessage(eventResponse.toString()));
				}
				play.isEvent((Event) storyCard);
				updateCards();
				Thread.sleep(5000);
				playerReady(session);
			}
		}
	}

	public void hostForQuest(WebSocketSession session, String message) throws IOException {
        //logIt.info("in host for quest with message: " + message);
        //logIt.info("order of players : "+orderOfPlayers.size());
		//orderOfPlayers.offer(session);
        boolean pCanSponsor = false;
        String sponsorName = "";
        if (RACING_ENABLED) {
            logIt.debug("race rigging is on");
            JsonObject response = new JsonObject();
            response.addProperty(METHOD_KEY, "canSponsorQuest");
            for (Player p : play.playerList) {
                logIt.info("in player loop");
                if (session.equals(p.getSession())) {
                    sponsorName = p.getName();
                    logIt.info("this person is the host" + p.getName());
                    if (message.equals("yes")) {
                        logIt.info("host has been determined and host is " + session.getId());
                        logIt.info("Player host is " + p.getName());
                        pCanSponsor = play.canSponsor(p);
                        if (pCanSponsor) {
                            logIt.info(p.getName() + " can host");
                            response.addProperty(BODY_KEY, "yes");
                        } else {
                            response.addProperty(BODY_KEY, "no");
                        }
                        session.sendMessage(new TextMessage(response.toString()));
                    }
                }
            }
            JsonObject responseOtherPlayers = new JsonObject();
            responseOtherPlayers.addProperty(METHOD_KEY, "sponsorFound");
            for (Player p : play.playerList) {
                if (!session.equals(p.getSession())) {
                    if (pCanSponsor) {
                        responseOtherPlayers.remove(BODY_KEY);
                        responseOtherPlayers.addProperty(BODY_KEY, sponsorName);
                        responseOtherPlayers.addProperty("sponsorFound", "yes");
                        notify(p.getSession(), "success", ""+sponsorName+" can sponsor");
                    } else {
                        responseOtherPlayers.remove(BODY_KEY);
                        responseOtherPlayers.addProperty(BODY_KEY, sponsorName);
                        responseOtherPlayers.addProperty("sponsorFound", "no");
                        notify(p.getSession(), "error", ""+sponsorName+" can't sponsor");
                    }
                    notify(p.getSession(), "success", ""+sponsorName+" is sponsoring quest and setting up stages");
                    p.getSession().sendMessage(new TextMessage(responseOtherPlayers.toString()));
                }
            }
        }
        else{
            logIt.debug("race rigging is off");
          //  logIt.debug("message recieving " + message);
            JsonObject response = new JsonObject();
            response.addProperty(METHOD_KEY, "questStarted");
            if(message.equals("yes")){
                //check if he can host
                boolean playerCanSponsor = false;
                logIt.debug("inside yes");
                for(Player p: play.playerList){
                    if(p.getSession().equals(session)){
                        playerCanSponsor = play.canSponsor(p);
                    }
                }
                if(playerCanSponsor){
                    ////start setting up stages
                    response.remove(METHOD_KEY);
                    response.addProperty(METHOD_KEY, "canSponsorQuest");
                    response.addProperty(BODY_KEY, "yes");
                    logIt.debug("Player can sponsor");
                    session.sendMessage(new TextMessage(response.toString()));
					for(Player p: play.playerList){
						if(!p.getSession().equals(session)){
							notify(p.getSession(), "success", ""+ sponsorName +" is sponsoring the game and will now setup the stages");
						}
					}
                }else {
                    //if(askedPlayersToSponsorQuest == )
                    //check to see if askedPlayers is greater than number of players
					notify(session, "error", "Unfortunately you can't sponsor the quest.");
                    if(askedPlayersToSponsorQuest == TOTAL_PLAYERS){
						for(Player p: play.playerList){
							notify(p.getSession(), "info", "Everyone declined to sponsor the quest, Drawing next story card");
						}
						try {
							playerReady(session);
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else {
                        response.remove(METHOD_KEY);
                        response.remove(BODY_KEY);
                        response.addProperty(METHOD_KEY, "questStarted");
                        response.addProperty(BODY_KEY, currentStory.GetName());
                        response.addProperty(NUM_STAGES, ((Quest) currentStory).GetNumStages());
                        play.playerList.get(play.askNext() - 1).getSession().sendMessage(new TextMessage(response.toString()));
                        //askedPlayersToParticipateInQuest++;
                    }
                }

            }
            else if(message.equals("no")){
                //check to see if askedPlayers is greater than number of players
				sponsorName = "";
                if(askedPlayersToSponsorQuest == TOTAL_PLAYERS){
					try {
						playerReady(session);
					} catch (Exception e) {
						e.printStackTrace();
					}
					for(Player p: play.playerList){
						notify(p.getSession(), "info", "Everyone declined to sponsor the quest, Drawing next story card");
					}
                } else {
					for(Player p: play.playerList){
						if(p.getSession().equals(session)){
							sponsorName = p.getName();
							break;
						}
					}
                    //logIt.debug("session id is " + session.getId());
                    //logIt.debug("inside no statment");
                    //logIt.debug("player name to ask next : " + play.playerList.get(play.askNext() - 1).getName());
                    response.addProperty(BODY_KEY, currentStory.GetName());
                    response.addProperty(NUM_STAGES, ((Quest) currentStory).GetNumStages());
                    int forLogging = play.playerTurn;
                    //logIt.debug("Player currently being asked is "+forLogging);
					for(Player p: play.playerList){
						if(!p.getSession().equals(session)){
							notify(p.getSession(), "warning", ""+ sponsorName +" declined to sponsor the quest");
						}
					}
                    play.playerList.get(play.askNext() - 1).getSession().sendMessage(new TextMessage(response.toString()));
                    askedPlayersToSponsorQuest++;
                }
            }
            else {
                logIt.error("CRITICAL ERROR went inside else host for quest");
            }
        }
    }

    public void cardsForQuest(WebSocketSession session, Map<String, String> message) throws Exception{
        logIt.debug("session is : "+session.getId());
	    logIt.debug("Inside cardsForQuest in the actual method");
        playersParticipating.clear();
        orderOfPlayersToAskQuest.clear();
        //logIt.debug("made it past the beginning stuff");
		ArrayList<Pair<Integer, Integer>> stageAndIndex = new ArrayList<>();
		Quest questCard = (Quest) play.discardStoryDeck.get(play.discardStoryDeck.size() - 1);
		//logIt.debug("made it past the beginning stuff");
		for(int i = 1; i <= questCard.GetNumStages(); i++){
			//logIt.debug("i in cardsForQuest is " + i);
			String key = "stage" + i + "";
			String entry = message.get(key);
			logIt.debug("This is entry: " + entry);
			String [] tempArr = {"[", "]"};
			entry = play.entryParser(entry, tempArr);
			logIt.debug("This is entry now: " + entry);
			String [] tokens = entry.split("[,]");
			for(int j = 0; j < tokens.length; j++){
				try {
					int parsedInt = Integer.parseInt(tokens[j]);
					stageAndIndex.add(new Pair<>(i, parsedInt));
				}catch(Exception e) {
					e.printStackTrace();
					logIt.error("Critical error in cardsForQuest, couldn't convert int");
					return;
				}
			}
		}
		Collections.sort(stageAndIndex, new Comparator<Pair<Integer, Integer>>() {
			@Override
			public int compare(Pair<Integer, Integer> o1, Pair<Integer, Integer> o2) {
				return o2.getSecond() - o1.getSecond();//to sort in descending order
			}
		});

		JsonObject response = new JsonObject();

		//response.addProperty(BODY_KEY, cardsAndBattlePoints.toString());
        String hostName = "";
        int playerNumber = 0;
        for(int i=0; i<play.playerList.size(); i++){
            if(play.playerList.get(i).getSession().equals(session)){
                hostName = play.playerList.get(i).getName();
                sponsorForQuest = play.playerList.get(i);
                playerNumber = i;
            }
        }

        cardsAndBattlePoints = play.setUpStages(stageAndIndex, questCard, sponsorForQuest);
		updateCards();

        logIt.debug("playerNumber is "+ playerNumber);
        //logIt.debug("orderOfPlayersSize is "+ orderOfPlayersToAskQuest.size());
        if(playerNumber == 0){
            if(TOTAL_PLAYERS > 1)
                orderOfPlayersToAskQuest.add(1);
            	playerBids.offer(play.playerList.get(1).getSession());
            if(TOTAL_PLAYERS > 2) {
                orderOfPlayersToAskQuest.add(2);
                playerBids.offer(play.playerList.get(2).getSession());
            }
            if(TOTAL_PLAYERS > 3) {
                orderOfPlayersToAskQuest.add(3);
				playerBids.offer(play.playerList.get(3).getSession());
            }
        } else if(playerNumber == 1){
            if(TOTAL_PLAYERS > 1)
                orderOfPlayersToAskQuest.add(2);
				playerBids.offer(play.playerList.get(2).getSession());
            if(TOTAL_PLAYERS > 2) {
                orderOfPlayersToAskQuest.add(3);
				playerBids.offer(play.playerList.get(3).getSession());
            }
            if(TOTAL_PLAYERS > 3) {
                orderOfPlayersToAskQuest.add(0);
				playerBids.offer(play.playerList.get(0).getSession());
            }
        } else if(playerNumber == 2){
            if(TOTAL_PLAYERS > 1)
                orderOfPlayersToAskQuest.add(3);
				playerBids.offer(play.playerList.get(3).getSession());
            if(TOTAL_PLAYERS > 2) {
                orderOfPlayersToAskQuest.add(0);
				playerBids.offer(play.playerList.get(0).getSession());
            }
            if(TOTAL_PLAYERS > 3) {
                orderOfPlayersToAskQuest.add(1);
				playerBids.offer(play.playerList.get(1).getSession());
            }
        } else if(playerNumber == 3){
            if(TOTAL_PLAYERS > 1)
                orderOfPlayersToAskQuest.add(0);
				playerBids.offer(play.playerList.get(0).getSession());
            if(TOTAL_PLAYERS > 2) {
                orderOfPlayersToAskQuest.add(1);
				playerBids.offer(play.playerList.get(1).getSession());
            }
            if(TOTAL_PLAYERS > 3) {
                orderOfPlayersToAskQuest.add(2);
				playerBids.offer(play.playerList.get(2).getSession());
            }
        }

        //logIt.debug("order of players to ask quest size after running logic is "+orderOfPlayersToAskQuest.size());
        //logIt.debug("printing out order of players to ask next");
        for(Integer playerOrder: orderOfPlayersToAskQuest){
            System.out.println(playerOrder);
        }


        if(RACING_ENABLED) {
            for (WebSocketSession webSocketSession : sessions) {
                if (!(webSocketSession.equals(session))) {
                    response.addProperty(METHOD_KEY, "participateInQuest");
                    response.addProperty(BODY_KEY, currentStory.GetName());
                    response.addProperty("questHost", hostName);
                    webSocketSession.sendMessage(new TextMessage(response.toString()));
                } else if (webSocketSession.equals(session)) {
                    response.addProperty(METHOD_KEY, "askingOtherPlayersToParcipateInQuest");
                    webSocketSession.sendMessage(new TextMessage(response.toString()));
                }
            }
        }
        else{
            response.addProperty(METHOD_KEY, "participateInQuest");
            response.addProperty(BODY_KEY, currentStory.GetName());
            response.addProperty("questHost", hostName);
            int nextPlayerToAsk = orderOfPlayersToAskQuest.get(0);
           // logIt.debug("player to ask next is : " +play.askNext());
			Player nextPlayer = play.playerList.get(nextPlayerToAsk);
			for(Player p: play.playerList) {
				notify(p.getSession(), "info", "" + nextPlayer.getName() + "'s" + " turn");
			}
            nextPlayer.getSession().sendMessage(new TextMessage(response.toString()));
        }


	}

	public void playersParticipatingInQuest(WebSocketSession session, String message) throws Exception{
		//TODO talk to naseer about method names in logger
		String playerName = "";
		String playerName2 = "";
		for(int i =0; i < play.playerList.size(); i++) {
			if (play.playerList.get(i).getSession().equals(session)) {
				System.out.println("THIS IS i " + i);
				//playerName = play.playerList.get(i+1).getName();
				playerName2 = play.playerList.get(i).getName();
			}
		}
		for(Player p: play.playerList) {
			//if (!p.getSession().equals(session)) {
			notify(p.getSession(), "warning", "Its " + playerName + "s" + " turn");

			//}
		}

	    //TODO - players not participating should be able to spectate
	    if(message.equals("yes")){
			for(Player p: play.playerList) {
				if (!p.getSession().equals(session)) {
					notify(p.getSession(),"info", "" + playerName2 + " is participating in the quest");
				}
			}
            for(Player p: play.playerList){
                if(p.getSession().equals(session)){
                    p.cardsInHand.add((Adventure) play.draw("adventure"));
                    JsonObject response = updateCardsInHand(p);
                    playersParticipating.add(p);
                    response.addProperty(METHOD_KEY, "updateCardsForQuest");
                    session.sendMessage(new TextMessage(response.toString())); //sending updated cards to the server
                }
            }
            //logIt.info("playersParticipating size is "+playersParticipating.size());
        } else {
            logIt.info("Player not participating");
			for(Player p: play.playerList) {
				if (!p.getSession().equals(session)) {
					notify(p.getSession(),"info", "" + playerName  + " is not participate in this quest");
				}
			}
			//logIt.info("Player not participating");
        }
        askedPlayersToParticipateInQuest++;
	    logIt.debug("askedPlayersToParticipateInQuest : "+askedPlayersToParticipateInQuest);
	    boolean currentCardIsTest = false;
        if(askedPlayersToParticipateInQuest == (TOTAL_PLAYERS - 1)){
        	updateCards();
            logIt.info("asked all players");
            askedPlayersToParticipateInQuest = 0;
            if(playersParticipating.size() != 0) {
				for (Player p : playersParticipating) {
					JsonObject response = new JsonObject();
					response.addProperty(METHOD_KEY, "questInProgress");
					int counter = 0;
					for (Map.Entry<ArrayList<Adventure>, Integer> entrySet : cardsAndBattlePoints.entrySet()) {
						if (entrySet.getValue() == -1 && counter == 0) {
							Test testCard = (Test) entrySet.getKey().get(0);
							response.addProperty("minBids", testCard.GetMinBids());
							playerBid = testCard.GetMinBids();
							response.addProperty("maxBids", 11);
							currentCardIsTest = true;

						}
						counter++;
					}
					response.addProperty("totalStages", ((Quest) currentStory).GetNumStages());
					response.addProperty("currentStage", 0);
					currentStageNow = 0;
					response.addProperty("isTest", currentCardIsTest);
					int nextPlayerToAsk = orderOfPlayersToAskQuest.get(askedPlayersToParticipateInQuest);
					if (currentCardIsTest) {
						logIt.debug("card is a test to ask players");
						play.playerList.get(nextPlayerToAsk).getSession().sendMessage(new TextMessage(response.toString()));
						return;
					} else {
						logIt.debug("card is not a test to ask players");
						p.getSession().sendMessage(new TextMessage(response.toString()));
					}
				}
			} else {
				int cardsUsed = 0;
				for(ArrayList<Adventure> a : cardsAndBattlePoints.keySet()){
					cardsUsed += a.size();
				}
				int cardsToDraw = ((Quest) currentStory).GetNumStages() + cardsUsed;
				for(int i = 0; i < cardsToDraw; i++){
					sponsorForQuest.cardsInHand.add((Adventure) play.draw("Adventure"));
				}
				updateCards();
				for(Player p:play.playerList){
					if(p != sponsorForQuest) {
						notify(p.getSession(), "info", "Nobody has accepted the quest and "+sponsorForQuest.getName() + " was awarded for "+ cardsUsed + " cards");
					} else {
						notify(p.getSession(), "warning", "Nobody decided to participate in your quest");
					}
				}
			}
        } else {
            JsonObject response = new JsonObject();
            response.addProperty(METHOD_KEY, "participateInQuest");
            response.addProperty(BODY_KEY, currentStory.GetName());
            response.addProperty("questHost", sponsorForQuest.getName());
            int nextPlayerToAsk = orderOfPlayersToAskQuest.get(askedPlayersToParticipateInQuest);
            logIt.debug("next Player to participate in quest is : "+nextPlayerToAsk);
            play.playerList.get(nextPlayerToAsk).getSession().sendMessage(new TextMessage(response.toString()));
        }
    }

    public JsonObject updateCardsInHand(Player p){

        JsonObject response = new JsonObject();

        JsonArray cardsInHandForPlayer = new JsonArray();
        for(Adventure card : p.cardsInHand){
            JsonObject individualCard = new JsonObject();
            individualCard.addProperty("name", card.GetName());
            individualCard.addProperty("battlepoints", card.GetBattlePoints());
            cardsInHandForPlayer.add(individualCard);
        }
        response.add(BODY_KEY, cardsInHandForPlayer);
        return response;
    }


    public void bidHandler(WebSocketSession session, Map<String, String> message, boolean bidWinnerFound, int currentStage) throws Exception {
		logIt.info("reachedBidHandler");
		if(!bidWinnerFound) {
			for (Player p : playersParticipating) {
				if (p.getSession() == session) {
					p.askedToBid = true;
					logIt.info("asked player");
				}
			}
			playerBids.remove(session);
			if (message.get("participating").equals("yes")) {
				logIt.info("player said yes");
				playerBid = Integer.parseInt(message.get("body"));
				if(playerBids.size() != 0) {
					playerBid++;
				}
				JsonObject response = new JsonObject();
				response.addProperty(METHOD_KEY, "questInProgress");
				response.addProperty("minBids", playerBid);
				response.addProperty("maxBids", 11);
				response.addProperty("isTest", true);
				response.addProperty("currentStage", currentStage);
				playerBids.offer(session);
				if (playerBids.size() == 1) {
					WebSocketSession temp = playerBids.pop();
					logIt.info("Last player for bidding");
					for (Player p : playersParticipating) {
						if (temp == p.getSession()) {
							if (p.askedToBid) {
								JsonObject newResponse = new JsonObject();
								newResponse.addProperty(METHOD_KEY, "questInProgress");
								newResponse.addProperty("minBids", playerBid);
								newResponse.addProperty("maxBids", 11);
								newResponse.addProperty("isTest", true);
								newResponse.addProperty("body", "lastBidderForced");
								newResponse.addProperty("currentStage", currentStage);
								temp.sendMessage(new TextMessage(newResponse.toString()));
							} else {
								response.addProperty("body", "lastBidder");
								temp.sendMessage(new TextMessage(response.toString()));
							}
						}
					}
				} else if (playerBids.size() == 0) {
					logIt.debug("no body wants to participate");
				} else {
					WebSocketSession temp = playerBids.pop();
					temp.sendMessage(new TextMessage(response.toString()));
				}
			}

			if (message.get("participating").equals("no")) {
				for(Player p:play.playerList){
					if(p.getSession() == session){
						playersParticipating.remove(p);
					}
				}
				logIt.info("player said no");
				JsonObject response = new JsonObject();
				response.addProperty(METHOD_KEY, "questInProgress");
				response.addProperty("minBids", playerBid);
				response.addProperty("maxBids", 11);
				response.addProperty("isTest", true);
				response.addProperty("currentStage", currentStage);
				if (playerBids.size() == 0) {
					logIt.debug("no body wants to participate");
				} else if (playerBids.size() == 1) {
					WebSocketSession temp = playerBids.pop();
					for (Player p : playersParticipating) {
						if (temp == p.getSession()) {
							if (p.askedToBid) {
								logIt.debug("player has already made a bid");
								JsonObject newResponse = new JsonObject();
								newResponse.addProperty(METHOD_KEY, "questInProgress");
								newResponse.addProperty("minBids", playerBid);
								newResponse.addProperty("maxBids", 11);
								newResponse.addProperty("isTest", true);
								newResponse.addProperty("body", "lastBidderForced");
								//response.addProperty("body", "lastBidder");
								newResponse.addProperty("currentStage", currentStage);
								temp.sendMessage(new TextMessage(newResponse.toString()));
							} else {
								response.addProperty("body", "lastBidder");
								logIt.debug("currentStageNow is "+currentStageNow++);
								logIt.debug(response.toString());
								temp.sendMessage(new TextMessage(response.toString()));
							}
						}
					}
				} else {
					playerBids.pop().sendMessage(new TextMessage(response.toString()));
				}
			}
		}else {
			updateCards();
			for(int i=0; i<play.playerList.size(); i++){
				if(play.playerList.get(i).getSession() == session){
					Player temp = play.playerList.get(i);
					//logIt.debug("indexes are: "+message.get("body"));
					String indexes = message.get("body");
					//logIt.debug("This is indexes " + indexes);
					String[] tempArr = {"[", "]"};
					indexes = play.entryParser(indexes, tempArr);
					//logIt.debug("This is indexes now " + indexes);
//					indexes = message.get("body");
//					indexes = indexes.substring(1,indexes.length()-1);
					play.removeFromPlayerHand(indexes,i,false);
					logIt.debug("playerHandSize is now "+play.playerList.get(i).cardsInHand.size());
					//updateCards(play.playerList.get(i));
					cardsUsedForQuest(session,null, message.get("currentStage"),true);
				}
			}
		}
    }


    public void cardsUsedForQuest(WebSocketSession session, String body, String currentStage, boolean cameFromTest) throws Exception{
	    //TODO - implement reseting boolean for all players
		//logIt.debug("**************************************************************************************************************************************************************************************************************************************************************");
        //logIt.debug("currentStage coming from server is : "+currentStage);
		//logIt.debug("**************************************************************************************************************************************************************************************************************************************************************");
	    try {
            currentStageNow = Integer.parseInt(currentStage);
        } catch (Exception e){
            e.printStackTrace();
            logIt.error("Critical error in converting to an int to currentStage");
        }
       	updateCards();
        //logIt.debug("--------------------------------------------------------------->");
        //logIt.debug("current stage now to check is "+currentStageNow);
        //logIt.debug("<-------------------------------------------------------------->");
		if(cameFromTest && playersParticipating.size() == 0){
			logIt.debug("the quest is done");
			JsonObject testing = new JsonObject();
			testing.addProperty(METHOD_KEY, "questCompleted");
			logIt.info("<----> all stages complete <------>");
			for(Player p: play.playerList){
				notify(p.getSession(), "info", "Nobody has won");
			}
			int cardsUsed = 0;
			for(ArrayList<Adventure> a : cardsAndBattlePoints.keySet()){
				cardsUsed += a.size();
			}

			int cardsToDraw = ((Quest) currentStory).GetNumStages() + cardsUsed;
			for(int i = 0; i < cardsToDraw; i++){
				sponsorForQuest.cardsInHand.add((Adventure) play.draw("Adventure"));
			}
			for(Player p:play.playerList){
				notify(p.getSession(), "info", sponsorForQuest.getName()+ " has been awarded "+ cardsUsed);
			}
			playerReady(session);
			return;
		}
			updateCards();
			String indexes = "";
			STORY_PLAYERCOUNT++;
			if(body != null) {
				indexes = body;
				//logIt.debug("This is indexes " + indexes);
				String[] tempArr = {"[", "]"};
				indexes = play.entryParser(indexes, tempArr);
				//logIt.debug("This is indexes now " + indexes);
			}
			for (int i = 0; i < play.playerList.size(); i++) {
				if (play.playerList.get(i).getSession().equals(session)) {
					if(body != null) {
						play.removeFromPlayerHand(indexes, i, true);
						logIt.debug("size of player hand is " + play.playerList.get(i).cardsInHand.size());
						//updateCards(play.playerList.get(i));
					}
					int counter = 0;
					for (Map.Entry<ArrayList<Adventure>, Integer> entryForMap : cardsAndBattlePoints.entrySet()) {
						if (counter == currentStageNow && entryForMap.getValue() != -1) { //TODO i added check for where value is -1 - MNA
							logIt.info("Current stage is battle points is " + entryForMap.getValue());
							logIt.info("the cards in the adventure arraylist is ");
							for (Adventure card : entryForMap.getKey()) {
								//logIt.info("            card in the " + counter + " stage, is " + card.GetName());
							}
							logIt.debug("we are in the actual stage ");
							Integer value = entryForMap.getValue();
							System.out.println("Name: " + play.playerList.get(i).getName() + " | " + " Round Battle score " + play.playerList.get(i).roundBattleScore + " | " + " value " + value);
							if (play.playerList.get(i).roundBattleScore >= value) {
								play.playerList.get(i).setEliminated(false);
								logIt.info("player Name is : " + play.playerList.get(i).getName() + " and their elimination status is: " + play.playerList.get(i).getEliminatedValue());
							} else {
								play.playerList.get(i).setEliminated(true);
								logIt.info("player Name is : " + play.playerList.get(i).getName() + " and their elimination status is: " + play.playerList.get(i).getEliminatedValue());
							}
							updateCards();
						} else if (counter == currentStageNow && entryForMap.getValue() == -1) {
							logIt.info("This card is a test card");
							updateCards();
						}
						counter++;
					}

				}
			}
			if (STORY_PLAYERCOUNT == playersParticipating.size()) {
				updateCards();
				//TODO send back all player responses and next stage info, reset storyPLayerCount
				//stripCardsNotifier();
				STORY_PLAYERCOUNT = 0;
				logIt.info("all players have chosen their cards");
				currentStageNow++;
				System.out.println("current stage to be sent back to players not eliminated : " + currentStageNow);
				int counter = 0;
				boolean nextCardTest = false;
				Test temp = null;
				for (Map.Entry<ArrayList<Adventure>, Integer> entryForMap : cardsAndBattlePoints.entrySet()) {
					if (counter == currentStageNow) {
						if (entryForMap.getValue() == -1) {
							//logIt.debug("*******************************************************************************************************************************");
							logIt.debug("this stage is a test");
							nextCardTest = true;
							temp = (Test) entryForMap.getKey().get(0);
							temp.toString();
						}
					}
					counter++;
				}
				if (currentStageNow <= cardsAndBattlePoints.size()-1) {
					//System.out.println("current stage to be sent back to players not eliminated : " + currentStageNow);
					updateCards();
					for (Player p : play.playerList) {
						logIt.debug("the currentStageNow is " + currentStageNow);
						//updateCards(p);
						if (playersParticipating.contains(p)) {
							if (p.getEliminatedValue()) {
								notify(p.getSession(), "error", "Unfortunately you have lost the stage and have been eliminated");
								playersParticipating.remove(p);
								updateCards();
							} else {
								JsonObject response = new JsonObject();
								response.addProperty(METHOD_KEY, "questInProgress");
								response.addProperty("currentStage", currentStageNow);
								if(nextCardTest){
									response.addProperty("minBids", temp.GetMinBids());
									response.addProperty("maxBids", 11);
									response.addProperty("isTest", nextCardTest);
									playerBids.pop().sendMessage(new TextMessage(response.toString()));
									updateCards();
									return;
								} else {
									response.addProperty("isTest", nextCardTest);
									p.getSession().sendMessage(new TextMessage(response.toString()));
									updateCards();
								}
								System.out.print("this the current elimination status " + p.getEliminatedValue());
							}
						}
						p.roundBattleScore = 0;
						if(playersParticipating.size() == 0){
							for(Player player: play.playerList){
								notify(player.getSession(), "info", "Nobody has won");
							}
							int cardsUsed = 0;
							for(ArrayList<Adventure> a : cardsAndBattlePoints.keySet()){
								cardsUsed += a.size();
							}
							int cardsToDraw = ((Quest) currentStory).GetNumStages() + cardsUsed;
							for(int i = 0; i < cardsToDraw; i++){
								sponsorForQuest.cardsInHand.add((Adventure) play.draw("Adventure"));
							}
							for(Player player:play.playerList){
								notify(player.getSession(), "info", sponsorForQuest.getName()+ " has been awarded "+ cardsUsed);
							}
							updateCards();
							playerReady(session);
							return;
						}
					}
				} else {
					updateCards();
					JsonObject testing = new JsonObject();
					testing.addProperty(METHOD_KEY, "questCompleted");
					logIt.info("<----> all stages complete <------>");
					if(playersParticipating.size() != 0) {
						for (Player p : playersParticipating) {
							p.setShields(p.getShields() + ((Quest) currentStory).GetNumStages());
							notify(p.getSession(), "success", "Congrats you've won!");
							p.getSession().sendMessage(new TextMessage(testing.toString()));
						}

					}else{
						updateCards();
						for(Player p: play.playerList){
							notify(p.getSession(), "info", "Nobody has won");
						}
					}

					play.rankUp();
					int cardsUsed = 0;
					for(ArrayList<Adventure> a : cardsAndBattlePoints.keySet()){
						cardsUsed += a.size();
					}

					int cardsToDraw = ((Quest) currentStory).GetNumStages() + cardsUsed;
					for(int i = 0; i < cardsToDraw; i++){
						sponsorForQuest.cardsInHand.add((Adventure) play.draw("Adventure"));
						updateCards();
					}
					for(Player p:play.playerList){
						notify(p.getSession(), "info", sponsorForQuest.getName()+ " has been awarded "+ cardsUsed);
					}
					playerReady(session);
				}
			}
    }

    public void updateCards() throws Exception{
		for(Player p:play.playerList) {
			JsonObject response = new JsonObject();
			response.addProperty(METHOD_KEY, "updateCardsForQuest");
			JsonArray cardsArr = new JsonArray();
			//logIt.debug("size of players cards from updateCards is " + p.cardsInHand.size());
			for (Adventure card : p.cardsInHand) {
				JsonObject cards = new JsonObject();
				cards.addProperty("name", card.GetName());
				cards.addProperty("battlePoints", card.GetBattlePoints());
				cardsArr.add(cards);
			}
			response.add(BODY_KEY, cardsArr);
			p.getSession().sendMessage(new TextMessage(response.toString()));
		}
	}

	public void notify(WebSocketSession session, String type, String message) throws IOException {
		JsonArray body = new JsonArray();
		for(Player p: play.playerList){
			JsonObject temp = new JsonObject();
			temp.addProperty("name", p.getName());
			temp.addProperty("rank", p.getRank());
			temp.addProperty("shields", p.getShields());
			temp.addProperty("cardsInHand",p.cardsInHand.size());
			body.add(temp);
		}
		JsonObject notification = new JsonObject();
		notification.addProperty(METHOD_KEY, "notify");
		notification.addProperty("type", type);
		notification.addProperty("message", message);
		notification.add(BODY_KEY, body);
		logIt.debug("response is "+notification.toString());
		session.sendMessage(new TextMessage(notification.toString()));
	}

	public int stripCardsNotifier(){
		int counter = 0;
		for(Player p:play.playerList){
			if(p.cardsInHand.size() > 12){
				counter++;
				JsonObject response = new JsonObject();
				response.addProperty(METHOD_KEY, "stripCards");
				int toRemoveCards = p.cardsInHand.size() - 12;
				response.addProperty(BODY_KEY, "Select " +toRemoveCards+ " cards to remove");
				try {
					p.getSession().sendMessage(new TextMessage(response.toString()));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		logIt.debug(counter + " player(s) have more than 12 cards");
		return counter;
	}

	public void stripCards(WebSocketSession session, Map<String, String> message){
		stripCardsCounter++;
		for(int i=0; i<play.playerList.size(); i++){
			if(play.playerList.get(i).getSession() == session){
				String indexes = message.get("body");
				//logIt.debug("This is indexes " + indexes);
				String[] tempArr = {"[", "]"};
				indexes = play.entryParser(indexes, tempArr);
				//logIt.debug("This is indexes now " + indexes);
				play.removeFromPlayerHand(indexes,i,false);
				logIt.info(play.playerList.get(i).getName() + " has "+ play.playerList.get(i).cardsInHand.size());
				try {
					updateCards();
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					playerReady(session);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

//	public void updateAlliesInPlay() throws IOException {
//		JsonObject response = new JsonObject();
//		response.addProperty(METHOD_KEY, "alliesInPlay");
//		//JsonArray cardsArr = new JsonArray();
//		for(Player p:play.playerList){
//			JsonArray playerCardArray = new JsonArray();
//			for(Ally card:p.alliesInPlay){
//				JsonObject cardTemp = new JsonObject();
//				cardTemp.addProperty("name", card.GetName());
//				playerCardArray.add(cardTemp);
//			}
//			response.add(p.getName(), playerCardArray);
//		}
//		for(WebSocketSession webSocketSession : sessions){
//			webSocketSession.sendMessage(new TextMessage(response.toString()));
//		}
//		//response.add(BODY_KEY, cardsArr);
//		logIt.debug("response is : "+response.toString());
//	}


}

