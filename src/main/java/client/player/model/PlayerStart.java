package client.player.model;

import CommonTypes.AuthResult;
import CommonTypes.InvalidSessionException;
import PlayerService.Player;

public class PlayerStart {
    private Player playerStub;
    private AuthResult user;

    public PlayerStart(Player playerStub, AuthResult user) {
        this.playerStub = playerStub;
        this.user = user;
    }

    public String createGame() throws InvalidSessionException {
        return  playerStub.createGame(user.token);
    }

    public Player getPlayerStub() {
        return playerStub;
    }

    public void setPlayerStub(Player playerStub) {
        this.playerStub = playerStub;
    }

    public AuthResult getUser() {
        return user;
    }

    public void setUser(AuthResult user) {
        this.user = user;
    }
}
