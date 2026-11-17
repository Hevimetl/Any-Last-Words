package client.player.model;

import CommonTypes.*;
import PlayerService.Player;


public class PlayerGame {
    private Player playerStub;
    private AuthResult user;
    private GameInfo currentGame;

    public PlayerGame(Player playerStub, AuthResult user, GameInfo currentGame) {
        this.playerStub = playerStub;
        this.user = user;
        this.currentGame = currentGame;

    }

    public int getRoundTime() throws GameNotFoundException {
        return playerStub.getRoundTime(user.token, currentGame.gameId);
    }

    public AuthResult getUser() {
        return user;
    }

    public GameInfo getCurrentGame() {
        return currentGame;
    }

    public GameInfo getGameDetails()  {
        return playerStub.getGameInfo(user.token, currentGame.gameId);
    }

    public Player getPlayerStub() {
        return playerStub;
    }

    public int[] sendGuess(char guess) throws GuessLimitReachedException, InvalidGuessException, LetterAlreadyGuessedException {
       return playerStub.sendGuess(user.token, currentGame.gameId, guess);
    }

    public String getFullWord() {
        return playerStub.getFullWord(user.token, currentGame.gameId);
    }
}
