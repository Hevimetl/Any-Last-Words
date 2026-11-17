package client.player.controller;

import client.player.model.PlayerGame;
import client.player.view.PlayerGameView;
import client.utility.CallbackImpl;
import client.utility.SceneManager;
import CommonTypes.AuthResult;
import CommonTypes.GameInfo;
import CommonTypes.GameNotFoundException;
import PlayerService.Player;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import org.omg.CORBA.ORB;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class PlayerGameController {
    private PlayerGame model;
    private PlayerGameView view;
    private Thread viewUpdater;
    private ORB orb;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> roundTimerTask;
    private int currentRound = 1; // Start at 1


    public PlayerGameController(Player playerStub, AuthResult user, GameInfo currentGame, ORB orb){
        model = new PlayerGame(playerStub, user, currentGame);
        view = new PlayerGameView();
        attachListeners();
        startAutoUpdate(3000);
        view.initializeHiddenWord(model.getCurrentGame());
        CallbackImpl callbackImpl = new CallbackImpl(this, orb, user);
        playerStub.registerCallback(model.getCurrentGame().gameId, model.getUser().token, callbackImpl.getCorbaReference());

        startRoundTimeSync();
    }

    //CHANGE THIS TESTER ONLY (Only update Health Bars and Timer)
    //Or Callback
    private void startAutoUpdate(long intervalMillis) {
        viewUpdater = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    SwingUtilities.invokeLater(this::updateView);

                    Thread.sleep(intervalMillis);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "View-Updater");
        viewUpdater.setDaemon(true);
        viewUpdater.start();
    }

    private void handleGameJoin(GameInfo game) {


    }

    private void updateView(){
        view.updatePlayerPanels(model.getGameDetails(), model.getUser().userName);
    }



    public void attachListeners(){
        view.setKeyPressHandler(this::handleKeyPress);
    }

    private void handleKeyPress(ActionEvent event) {


        Button sourceButton = (Button) event.getSource();
        String key = sourceButton.getText();
        char guess = key.charAt(0);

        try {
            int[] positions = model.getPlayerStub().sendGuess(
                    model.getUser().token,
                    model.getCurrentGame().gameId,
                    key.charAt(0)
            );

            List<Integer> positionsList = new ArrayList<>();
            for (int pos : positions) {
                positionsList.add(pos);
            }

            if (!positionsList.isEmpty()) {

                view.revealLetter(guess, positionsList);
            } else {
                // Incorrect guess
                view.playWrongGuessAnimation(String.valueOf(guess));
                view.updateDoor(model.getGameDetails(), model.getUser().userName);
                view.playDamageFlash();

                updateView();
            }


        } catch (Exception e) {
            view.showError("Guess Error", e.getMessage());
            sourceButton.setDisable(false);
            sourceButton.setStyle("-fx-base: #3a3a3a;");


        }


    }
    private void startRoundTimeSync() {
        // Cancel any existing task
        if (roundTimerTask != null) {
            roundTimerTask.cancel(false);
        }

        // Schedule new task to update round time every second
        roundTimerTask = scheduler.scheduleAtFixedRate(() -> {
            try {
                int timeLeft = model.getRoundTime();
                Platform.runLater(() -> view.updateRoundTimerLabel(timeLeft));

                // Check if round time has expired
                if (timeLeft <= 0) {
                    stopRoundTimer();
                    // The callback should handle the round end, but we can add additional logic here if needed
                }
            } catch (GameNotFoundException ex) {
                stopRoundTimer();
                Platform.runLater(() -> view.showError("Game Error", "Game not found!"));
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    private void stopRoundTimer() {
        if (roundTimerTask != null) {
            roundTimerTask.cancel(false);
            roundTimerTask = null;
        }
    }

    public void shutdown() {
        stopRoundTimer();
        scheduler.shutdown();
        if (viewUpdater != null) {
            viewUpdater.interrupt();
        }
    }

    public void handleRoundEnd(String winnerName) {
        Platform.runLater(() -> {
            String theWord = model.getFullWord();
            String currentPlayerName = model.getUser().userName;

            if (winnerName != null && !winnerName.equalsIgnoreCase("No winner")) {
                boolean isCurrentPlayerWinner = winnerName.equals(currentPlayerName);
                if (isCurrentPlayerWinner) {
                    view.setResultWinner(winnerName, theWord, currentRound);
                } else {
                    view.setResultLoser(theWord, winnerName, currentRound);
                }
            } else {
                // No winner case
                view.setResultLoser(theWord, "No winner", currentRound);
            }

            //updates the round
            currentRound++;

            // Start 5-second countdown
            view.startRoundCountdown(5, () -> {
                // This runs when countdown finishes
                Platform.runLater(() -> {
                    // Reset UI after countdown
                    view.resetKeyboard();
                    view.initializeHiddenWord(model.getGameDetails());
                    view.resetView();
                    startRoundTimeSync();
                    view.disableResult();
                });
            });

            SceneManager.resetMusic();
        });
    }

    public void handleGameEnd(String winnerName) {
        Platform.runLater(() -> {
            stopRoundTimer();
            stopAutoUpdate();
            disableRoundTimer();

            if (winnerName != null) {
                view.setGameWinner(winnerName);
            }
            stopAutoUpdate();
            disableRoundTimer();

            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                          @Override
                        public void run() {
                            Platform.runLater(() -> {
                                SceneManager.showScreen("PlayerMenu");
                                view.confetti.setImage(null); // Clear image
                                view.gameWinner.setImage(null);
                                view.gameEnd.setVisible(false);
                            });
                        }
                    },
                    5000 // Wait 5 seconds
            );

        });
    }

    public String getGameID(){
        return model.getCurrentGame().gameId;
    }

    private void stopAutoUpdate() {
        if (viewUpdater != null && viewUpdater.isAlive()) {
            viewUpdater.interrupt();
        }
    }

    private void disableRoundTimer(){
        stopRoundTimer();
        scheduler.shutdown();
        if (viewUpdater != null) {
            viewUpdater.interrupt();
        }
    }
}
