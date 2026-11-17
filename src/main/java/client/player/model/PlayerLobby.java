package client.player.model;

import CommonTypes.AuthResult;

import CommonTypes.GameInfo;
import CommonTypes.GameNotFoundException;
import CommonTypes.NoPlayersFoundException;
import PlayerService.Player;

public class PlayerLobby {
    private Player playerStub;
    private AuthResult user;
    private String gameID;

    public PlayerLobby(Player playerStub, AuthResult user, String gameID) {
        this.playerStub = playerStub;
        this.user = user;
        this.gameID = gameID;
    }

    public int getWaitingTime() throws GameNotFoundException, NoPlayersFoundException {
        return playerStub.getWaitingTime(user.token, gameID);
    }

    public GameInfo getGameDetails()  {
        return playerStub.getGameInfo(user.token, gameID);
    }

    public void setReady(){
        playerStub.markReady(user.token, gameID);
    }

    public Player getPlayerStub() {
        return playerStub;
    }

    public AuthResult getUser() {
        return user;
    }
}
