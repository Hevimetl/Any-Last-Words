package client.player.controller;

import client.player.model.PlayerGameSelection;
import client.player.view.PlayerGameSelectionView;
import client.utility.SceneManager;
import CommonTypes.AuthResult;
import CommonTypes.GameInfo;
import CommonTypes.NoPlayersFoundException;
import PlayerService.Player;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.util.Duration;
import org.omg.CORBA.ORB;

import java.util.List;

public class PlayerGameSelectionController {
    private static final int REFRESH_INTERVAL_SECONDS = 5;

    private PlayerGameSelection model;
    private PlayerGameSelectionView view;
    private Timeline gameRefreshTimeline;
    private ORB orb;

    public PlayerGameSelectionController(Player playerStub, AuthResult user, ORB orb) {
        this.model = new PlayerGameSelection(playerStub, user);
        this.view = new PlayerGameSelectionView();
        this.orb = orb;

        attachListeners();
        // show initial list
        view.displayGames(model.getAllJoinableGames());
        // start background refresh
        startGameRefresh();

        // handle double‐click to join
        view.setOnGameDoubleClicked(this::handleGameJoin);
    }

    private void startGameRefresh() {
        gameRefreshTimeline = new Timeline(
                new KeyFrame(Duration.seconds(REFRESH_INTERVAL_SECONDS), e -> refreshGames())
        );
        gameRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        gameRefreshTimeline.play();
    }

    private void refreshGames() {
        // fetch updated list off the FX thread
        List<GameInfo> games = model.getAllJoinableGames();
        // update UI on the FX thread
        Platform.runLater(() -> view.displayGames(games));
    }

    private void handleGameJoin(GameInfo game) {
        String gameId = game.gameId;
        try {
            model.joinGame(gameId);
            stopGameRefresh();

            new PlayerLobbyController(
                    model.getPlayerStub(),
                    model.getUser(),
                    gameId,
                    orb,
                    false // Set isHost to false when joining a game
            );
            SceneManager.showScreen("PlayerLobby");
        } catch (NoPlayersFoundException e) {
            view.showError("Join Failed", "Could not join the game: " + e.getMessage());
            refreshGames(); // Refresh the list after failed join attempt
        }
    }


    private void attachListeners() {
        view.setReturnHandler(this::handleReturn);
    }

    private void handleReturn(ActionEvent evt) {
        // stop refreshing before we go back
        stopGameRefresh();
        SceneManager.showScreen("PlayerStart");
    }

    private void stopGameRefresh() {
        if (gameRefreshTimeline != null) {
            gameRefreshTimeline.stop();
        }
    }
}
