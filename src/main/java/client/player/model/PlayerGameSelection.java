package client.player.model;

import CommonTypes.AuthResult;

import CommonTypes.GameInfo;
import CommonTypes.NoPlayersFoundException;
import PlayerService.Player;

import java.util.Arrays;
import java.util.List;

public class PlayerGameSelection {
    private Player playerStub;
    private AuthResult user;

    public PlayerGameSelection(Player playerStub, AuthResult user) {
        this.playerStub = playerStub;
        this.user = user;
    }

    public List<GameInfo> getAllJoinableGames(){
        return Arrays.asList(playerStub.getJoinableGames(user.token));
    }

    public Player getPlayerStub() {
        return playerStub;
    }

    public AuthResult getUser() {
        return user;
    }

    public void joinGame(String gameID) throws NoPlayersFoundException {
        playerStub.joinGame(user.token, gameID);
    }
}
