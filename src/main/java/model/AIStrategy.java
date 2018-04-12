package model;

//import static sun.audio.AudioPlayer.player;

public interface AIStrategy {
    //All methods, -1 if not going to do it. Not if otherwise
    int doIParticipateInTournament(AIPlayer player);
    int questSponsorShip(AIPlayer player);
    int doISponsorAQuest(AIPlayer player, int maxStageBP);
    int doIParticipateInQuest(AIPlayer player);
    int nextBid(AIPlayer player, int currentMaxBid);
    void discardAfterWinningTest(AIPlayer player);
    void discardIfHandFull();
}
