package client.player.controller;

import client.player.model.PlayerLeaderboard;
import client.player.view.PlayerLeaderboardView;
import client.utility.SceneManager;
import CommonTypes.AuthResult;
import PlayerService.Player;
import javafx.event.ActionEvent;
import javafx.scene.Parent;

public class PlayerLeaderboardController {
    private final PlayerLeaderboard model;
    private final PlayerLeaderboardView view;

    public PlayerLeaderboardController(Player playerStub, AuthResult user) {
        this.model = new PlayerLeaderboard(playerStub, user);
        this.view = new PlayerLeaderboardView();
        attachListeners();
        initialize();
    }

    private void initialize() {
        String[] topPlayers = model.getTopPlayers();
        view.displayTopPlayers(topPlayers);
    }

    private void attachListeners() {
        view.setReturnHandler(this::handleReturn);
    }

    private void handleReturn(ActionEvent evt) {
        SceneManager.showScreen("PlayerMenu");
    }

    public Parent getView() {
        return view.getRoot();
    }
}