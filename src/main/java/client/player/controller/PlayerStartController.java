package client.player.controller;

import client.player.model.PlayerStart;
import client.player.view.PlayerStartView;
import client.utility.SceneManager;
import javafx.event.ActionEvent;
import CommonTypes.AuthResult;
import CommonTypes.InvalidSessionException;
import PlayerService.Player;
import org.omg.CORBA.ORB;

public class PlayerStartController {
    private PlayerStart model;
    private PlayerStartView view;
    private ORB orb;

    public PlayerStartController(Player playerStub, AuthResult user, ORB orb) {
        model = new PlayerStart(playerStub, user);
        view = new PlayerStartView();
        this.orb = orb;
        attachListeners();
    }

    public void attachListeners() {
        view.setCreateHandler(this::handleCreate);
        view.setJoinHandler(this::handleJoin);
        view.setReturnHandler(this::handleReturn);
    }

    private void handleCreate(ActionEvent evt) {
        try {
            String gameID = model.createGame();
            PlayerLobbyController playerLobbyController = new PlayerLobbyController(
                    model.getPlayerStub(),
                    model.getUser(),
                    gameID,
                    orb,
                    true // Set isHost to true when creating a game
            );
            SceneManager.showScreen("PlayerLobby");
        } catch (InvalidSessionException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleJoin(ActionEvent evt) {
        PlayerGameSelectionController playerGameSelectionController = new PlayerGameSelectionController(
                model.getPlayerStub(),
                model.getUser(),
                orb
        );
        SceneManager.showScreen("PlayerGameSelection");
    }


    private void handleReturn(ActionEvent evt) {
        // Optionally, you can implement logic to leave the game session here if needed
        SceneManager.showScreen("PlayerMenu");
    }
}
