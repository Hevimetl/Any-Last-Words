package client.player.model;

import CommonTypes.AuthResult;
import PlayerService.Player;

public class PlayerMenu {

    private Player playerStub;
    private AuthResult user;
    public PlayerMenu(Player playerStub, AuthResult user){
        this.playerStub = playerStub;
        this.user = user;

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
