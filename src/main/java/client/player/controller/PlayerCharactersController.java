package client.player.controller;

import client.player.model.PlayerCharacters;
import client.player.view.PlayerCharactersView;
import client.utility.SceneManager;
import CommonTypes.AuthResult;
import CommonTypes.GameInfo;
import PlayerService.Player;
import javafx.event.ActionEvent;

public class PlayerCharactersController {
    private PlayerCharacters model;
    private PlayerCharactersView view;


    public PlayerCharactersController(Player playerStub, AuthResult user){
        model = new PlayerCharacters(playerStub, user);
        view = new PlayerCharactersView();
        attachListeners();

    }

    private void handleGameJoin(GameInfo game) {

    }

    public void attachListeners(){
        view.setReturnHandler(this::handleReturn);
        view.setOmoriHandler(e -> changeCharacter("Omori"));
        view.setBangsHandler(e -> changeCharacter("Bangs"));
        view.setGothHandler(e -> changeCharacter("Goth"));
        view.setSmirkHandler(e -> changeCharacter("Smirk"));
        view.setRuggedHandler(e -> changeCharacter("Rugged"));
        view.setSteveHandler(e -> changeCharacter("Steve"));
        view.setAmogusHandler(e -> changeCharacter("Amogus"));
        view.setMakilHandler(e -> changeCharacter("Makil"));
    }

    private void handleReturn(ActionEvent evt){
        SceneManager.showScreen("PlayerMenu");
    }


    private void changeCharacter(String character) {
        view.showMessage("Successful Character Change", "Successfully Changed Character to: " + character);
        model.changeCharacter(character);
    }

}
