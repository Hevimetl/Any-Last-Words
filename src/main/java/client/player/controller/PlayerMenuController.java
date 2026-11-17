package client.player.controller;

import client.player.model.PlayerMenu;
import client.player.view.PlayerMenuView;
import client.utility.CorbaHelper;
import client.utility.SceneManager;
import CommonTypes.AuthResult;
import PlayerService.Player;
import javafx.event.ActionEvent;
import client.Client;

public class PlayerMenuController {
    private PlayerMenu model;
    private PlayerMenuView view;

    public PlayerMenuController(Player playerStub, AuthResult user){
        model = new PlayerMenu(playerStub, user);
        view = new PlayerMenuView();
        attachListeners();
    }

    public void attachListeners(){
        view.setExitHandler(this::handleExit);
        view.setStartHandler(this::handleStart);
        view.setCharactersHandler(this::handleCharacters);
        view.setLeaderboardHandler(this::handleLeaderboard);

    }

    private void handleExit(ActionEvent evt) {
        if (view.confirmExit()) {
            try {
                Client.loginImpl.logoutAccount(model.getUser().token);
            } catch (Exception e) {
                view.showError("Logout Error", "Failed to logout: " + e.getMessage());
            }
            SceneManager.showScreen("auth");
        }
    }

    private void handleCharacters(ActionEvent evt){
        SceneManager.showScreen("PlayerCharacters");
    }

    private void handleStart(ActionEvent evt){
        PlayerStartController playerStartController = new PlayerStartController(model.getPlayerStub(), model.getUser(), CorbaHelper.getOrb());
        SceneManager.showScreenWithDelay("PlayerStart", 2);
    }

    private void handleLeaderboard(ActionEvent evt){
        SceneManager.showScreen("PlayerLeaderboard");
    }

}