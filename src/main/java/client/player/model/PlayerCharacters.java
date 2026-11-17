package client.player.model;

import CommonTypes.AuthResult;
import PlayerService.Player;

public class PlayerCharacters {
    private Player playerStub;
    private AuthResult user;

    public PlayerCharacters(Player playerStub, AuthResult user) {
        this.playerStub = playerStub;
        this.user = user;
    }



    public AuthResult getUser() {
        return user;
    }

    public void changeCharacter(String character){
        playerStub.changeCharacter(user.token, character);
    }


}
