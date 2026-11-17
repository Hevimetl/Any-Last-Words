package client.player.controller;

import CommonTypes.*;
import client.player.model.PlayerLobby;
import client.player.view.PlayerLobbyView;
import client.utility.SceneManager;
import PlayerService.Player;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import org.omg.CORBA.ORB;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class PlayerLobbyController {
    private PlayerLobby model;
    private PlayerLobbyView view;
    private Thread viewUpdater;
    private ORB orb;
    private boolean isHost;
    private final AtomicBoolean waitingForResponse = new AtomicBoolean(false);
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final AtomicBoolean shouldContinue = new AtomicBoolean(false);
    private final AtomicInteger remainingTimeWhenPaused = new AtomicInteger(0);

    public PlayerLobbyController(Player playerStub, AuthResult user, String gameID, ORB orb, boolean isHost) {
        this.model = new PlayerLobby(playerStub, user, gameID);
        this.view = new PlayerLobbyView();
        this.orb = orb;
        this.isHost = isHost;
        attachListeners();
        startViewUpdateLoop();
    }

    public void attachListeners() {
        view.setReadyHandler(this::handleReady);
        view.setReturnHandler(this::handleReturn);
    }

    private void handleReady(ActionEvent evt) {
        model.setReady();
    }

    private void startViewUpdateLoop() {
        viewUpdater = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        // Skip processing if waiting for user response
                        if (waitingForResponse.get()) {
                            Thread.sleep(100);
                            continue;
                        }

                        // Get current game state
                        GameInfo gameInfo = model.getGameDetails();
                        int timeLeft = model.getWaitingTime();
                        int readyCount = countReadyPlayers(gameInfo);

                        // Update UI
                        Platform.runLater(() -> {
                            view.updateTimerLabel(timeLeft);
                            view.updatePlayerList(gameInfo);
                        });
                        if (readyCount >= 2) {
                            Platform.runLater(this::startGame);
                            break;
                        }



                        Thread.sleep(1000); // Normal 1 second update
                    } catch (NoPlayersFoundException e) {
                        if (!waitingForResponse.get()) {
                            waitingForResponse.set(true);
                            Platform.runLater(() -> showWaitPrompt());

                        }
                    } catch (GameNotFoundException e) {
                        Platform.runLater(() -> {
                            view.showError("Game Error", "The game session was not found");
                            handleReturn(null);
                        });
                        break;
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            } catch (Exception e) {
                Thread.currentThread().interrupt();
            }
        }, "Lobby-View-Updater");

        viewUpdater.setDaemon(true);
        viewUpdater.start();
    }

    private void showWaitPrompt() {
        boolean userWantsToWait = view.showWaitPrompt();

        if (userWantsToWait) {
            try {
                model.getPlayerStub().waitForOtherPlayers(
                        model.getUser().token,
                        model.getGameDetails().gameId
                );
                waitingForResponse.set(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            shouldContinue.set(false);
            Platform.runLater(() -> handleReturn(null));
        }
    }




    private int countReadyPlayers(GameInfo gameInfo) {
        int count = 0;
        if (gameInfo.players != null) {
            for (PlayerGameState player : gameInfo.players) {
                if (player.isReady) count++;
            }
        }
        return count;
    }

    private void startGame() {
        stopViewUpdater();
        PlayerGameController controller = new PlayerGameController(model.getPlayerStub(), model.getUser(), model.getGameDetails(), orb);
        SceneManager.showScreen("PlayerGame");
    }

    public void shutdown() {
        stopViewUpdater();
        ScheduledExecutorService scheduler1 = scheduler;
        scheduler1.shutdownNow();
        stopViewUpdater();
    }

    private void stopViewUpdater() {
        if (viewUpdater != null) {
            viewUpdater.interrupt();
            viewUpdater = null;
        }
    }

    private void handleReturn(ActionEvent evt) {
        stopViewUpdater();
        try {
            if (isHost) {
                model.getPlayerStub().cancelGameRequest(model.getUser().token, model.getGameDetails().gameId);
            } else {
                model.getPlayerStub().leaveGame(model.getUser().token, model.getGameDetails().gameId);
            }
        } catch (Exception e) {
           view.showError("Error leaving game: ", e.getMessage());
        }
        SceneManager.showScreen("PlayerStart");
    }
}