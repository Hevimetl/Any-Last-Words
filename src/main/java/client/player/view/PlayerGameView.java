package client.player.view;

import client.utility.SceneManager;
import CommonTypes.GameInfo;
import CommonTypes.PlayerGameState;
import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

public class PlayerGameView {
    private final Parent root;

    // Keyboard elements
    @FXML private VBox keyboardVBox;
    @FXML private HBox topHBox;
    @FXML private HBox midHBox;
    @FXML private HBox bottomHBox;
    @FXML private HBox hiddenWord;


    // <editor-fold desc="Keyboard Configuration">
    private static final String[] TOP_ROW    = {"Q","W","E","R","T","Y","U","I","O","P"};
    private static final String[] MID_ROW    = {"A","S","D","F","G","H","J","K","L"};
    private static final String[] BOTTOM_ROW = {"Z","X","C","V","B","N","M"};
    private static final double KEY_SIZE     = 45.0;
    private static final double KEY_SPACING  = 4.0;
// </editor-fold>

    // <editor-fold desc="Players Configuration">
    @FXML private Pane player2_Panel;
    @FXML private Pane player3_Panel;
    @FXML private Pane player4_Panel;
    @FXML private Pane player5_Panel;

    @FXML private ImageView player1_Char;
    @FXML private ImageView player2_Char;
    @FXML private ImageView player3_Char;
    @FXML private ImageView player4_Char;
    @FXML private ImageView player5_Char;

    @FXML private Label player2_Name;
    @FXML private Label player3_Name;
    @FXML private Label player4_Name;
    @FXML private Label player5_Name;

    @FXML private  ImageView player1_Health;
    @FXML private ImageView player2_Health;
    @FXML private ImageView player3_Health;
    @FXML private ImageView player4_Health;
    @FXML private ImageView player5_Health;

    @FXML private ImageView doorPhase;
    @FXML private ImageView staticGIF;
    @FXML private ImageView questionerCreep;
    @FXML private ImageView questionerJumpscare;
    @FXML private ImageView background;
    @FXML private  Label damageLabel;

    // </editor-fold>

    @FXML private Label roundTimerLabel;
    @FXML private Pane roundResult;
    @FXML private ImageView roundWinner;
    @FXML private ImageView roundLoser;
    @FXML private Label theWordIs;
    @FXML private Label roundWinnerName;
    @FXML private Label roundTimerResult;
    @FXML public Label round;

    @FXML public Pane gameEnd;
    @FXML public ImageView confetti;
    @FXML public ImageView gameWinner;
    @FXML public Label gameWinnerName;

    private AudioClip damageSfx;
    private AudioClip deathSfx;



    private EventHandler<ActionEvent> keyPressHandler;

    public PlayerGameView() {
        try {
            FXMLLoader fxml = new FXMLLoader(getClass().getResource("/PlayerGame.fxml"));
            fxml.setController(this);
            root = fxml.load();
            SceneManager.addScreen("PlayerGame", root);

            root.getStylesheets().add(getClass().getResource("/styles/keyboard-style.css").toExternalForm());
            Font.loadFont(getClass().getResourceAsStream("/fonts/yoster.ttf"), 14);
            initializeKeyboard();
            initializeTimerLabel(); // Add this line

            damageSfx = new AudioClip(
                    getClass().getResource("/sounds/damage_sfx.mp3")
                            .toExternalForm()
            );

            deathSfx = new AudioClip(
                    getClass().getResource("/sounds/death_sfx.wav")
                            .toExternalForm()
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Add this method to initialize the timer label
    public void initializeTimerLabel() {
        if (roundTimerLabel != null) {
            roundTimerLabel.setFont(Font.font("Yoster Island Regular", FontWeight.BOLD, 24));
            roundTimerLabel.setTextFill(Color.YELLOW);
            roundTimerLabel.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5); " +
                    "-fx-background-radius: 10; " +
                    "-fx-padding: 5 15 5 15;");
            roundTimerLabel.setAlignment(Pos.CENTER);
        }
    }

    // Add this method to update the timer display
    public void updateRoundTimerLabel(int secondsRemaining) {
        if (roundTimerLabel != null) {
            // Format as MM:SS
            int minutes = secondsRemaining / 60;
            int seconds = secondsRemaining % 60;
            String timeString = String.format("%02d:%02d", minutes, seconds);

            roundTimerLabel.setText(timeString);

            // Change color when time is running low
            if (secondsRemaining <= 10) {
                roundTimerLabel.setTextFill(Color.RED);
                // Add pulse animation for low time
                if (roundTimerLabel.getEffect() == null) {
                    ScaleTransition pulse = new ScaleTransition(Duration.millis(500), roundTimerLabel);
                    pulse.setFromX(1.0);
                    pulse.setFromY(1.0);
                    pulse.setToX(1.1);
                    pulse.setToY(1.1);
                    pulse.setAutoReverse(true);
                    pulse.setCycleCount(Animation.INDEFINITE);
                    pulse.play();
                }
            } else {
                roundTimerLabel.setTextFill(Color.WHITE);
                roundTimerLabel.setEffect(null); // Remove any existing effects
                roundTimerLabel.setScaleX(1.0);
                roundTimerLabel.setScaleY(1.0);
            }
        }
    }

    public void startRoundCountdown(int seconds, Runnable onFinish) {
        // Reset and show the timer label
        roundTimerResult.setText(String.valueOf(seconds));
        roundTimerResult.setVisible(true);

        // Create the countdown animation
        Timeline countdown = new Timeline(
                new KeyFrame(Duration.seconds(1), event -> {
                    int remaining = Integer.parseInt(roundTimerResult.getText()) - 1;
                    roundTimerResult.setText(String.valueOf(remaining));

                    // Add visual effects
                    if (remaining <= 3) {
                        roundTimerResult.setTextFill(Color.RED);
                        ScaleTransition pulse = new ScaleTransition(Duration.millis(500), roundTimerResult);
                        pulse.setFromX(1.0);
                        pulse.setFromY(1.0);
                        pulse.setToX(1.2);
                        pulse.setToY(1.2);
                        pulse.setAutoReverse(true);
                        pulse.setCycleCount(2);
                        pulse.play();
                    }
                })
        );

        countdown.setCycleCount(seconds);
        countdown.setOnFinished(event -> {
            roundTimerResult.setVisible(false);
            onFinish.run();
        });
        countdown.play();
    }

    // <editor-fold desc="Keyboard Configuration">
    private void initializeKeyboard() {
        // Clear any existing children (in case of SceneBuilder placeholders)
        topHBox.getChildren().clear();
        midHBox.getChildren().clear();
        bottomHBox.getChildren().clear();

        /*
        // Style the keyboard container
        keyboardVBox.setSpacing(KEY_SPACING);
        keyboardVBox.setStyle("-fx-background-color: transparent; -fx-alignment: center;");
         */
        keyboardVBox.getStyleClass().add("keyboard-container");
        topHBox.getStyleClass().addAll("keyboard-row", "top-row");
        midHBox.getStyleClass().addAll("keyboard-row", "mid-row");
        bottomHBox.getStyleClass().addAll("keyboard-row", "bottom-row");


        // Create keyboard rows
        createKeyboardRow(topHBox, TOP_ROW);
        createKeyboardRow(midHBox, MID_ROW);
        setupBottomRow();
    }

    private void createKeyboardRow(HBox row, String[] keys) {
        row.setSpacing(KEY_SPACING);
        row.setStyle("-fx-alignment: center;");

        for (String key : keys) {
            Button keyButton = createPixelKey(key);
            row.getChildren().add(keyButton);
        }
    }

    private void setupBottomRow() {
        bottomHBox.setSpacing(KEY_SPACING);
        bottomHBox.setStyle("-fx-alignment: center;");
        for (String key : BOTTOM_ROW) {
            bottomHBox.getChildren().add(createPixelKey(key));
        }


    }

    private Button createPixelKey(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("pixel-key");

        button.setOnAction(e -> {
            // Visual feedback
            button.setDisable(true);
            button.setStyle("-fx-base: #555555;");

            if (keyPressHandler != null) {
                keyPressHandler.handle(new ActionEvent(button, null));
            }
        });

        return button;
    }

    public void setKeyPressHandler(EventHandler<ActionEvent> handler) {
        this.keyPressHandler = handler;
    }

    public void resetKeyboard() {
        topHBox.getChildren().clear();
        midHBox.getChildren().clear();
        bottomHBox.getChildren().clear();
        // …then rebuild all rows from scratch
        initializeKeyboard();
    }
    // </editor-fold>

    // <editor-fold desc="Players Configuration">

    public void updatePlayerPanels(GameInfo gameInfo, String currentUsername) {
        // Reset all panels
        resetAllPanels();


        setupCurrentUserPanel(gameInfo, currentUsername);


        if (gameInfo != null && gameInfo.players != null) {
            int panelIndex = 2; // Start from player2_Panel

            for (PlayerGameState player : gameInfo.players) {

                if (player.userName.equals(currentUsername)) {
                    continue;
                }

                // Assign to panels
                switch (panelIndex) {
                    case 2:
                        setupPanel(player2_Panel, player2_Char, player2_Name, player2_Health, player);
                        break;
                    case 3:
                        setupPanel(player3_Panel, player3_Char, player3_Name, player3_Health, player);
                        break;
                    case 4:
                        setupPanel(player4_Panel, player4_Char, player4_Name, player4_Health, player);
                        break;
                    case 5:
                        setupPanel(player5_Panel, player5_Char, player5_Name, player5_Health, player);
                        break;
                }

                panelIndex++;
                if (panelIndex > 5) break; // Only support up to 4 other players
            }
        }
    }

    public void updateDoor(GameInfo gameInfo, String currentUsername) {
        if (gameInfo == null || gameInfo.players == null) {
            System.err.println("Game info or player list is null.");
            return;
        }

        for (PlayerGameState player : gameInfo.players) {
            if (player.userName.equals(currentUsername)) {
                int attempts = player.remainingAttempts;
                int doorPhaseNumber = attempts + 1;
                doorPhase.setVisible(true);
                String imagePath = "/images/Door_Phase" + doorPhaseNumber + ".gif";

                if (player.remainingAttempts >= 1 ){
                    damageSfx.play();
                } else if (player.remainingAttempts == 0){
                    deathSfx.play();
                }

                try {
                    InputStream is = getClass().getResourceAsStream(imagePath);
                    if (attempts == 1) {
                        SceneManager.speedUpMusic();
                        Image questionerImage = new Image("/images/QuestionerCreep.gif");
                        questionerCreep.setImage(questionerImage);
                        questionerCreep.setOpacity(0); // Start  invisible
                        questionerCreep.setVisible(true);


                        PauseTransition pauseBeforeFade = new PauseTransition(Duration.seconds(0.3));
                        pauseBeforeFade.setOnFinished(e -> {
                            Timeline fadeIn = new Timeline(
                                    new KeyFrame(Duration.millis(300), // Duration of fade in ms
                                            new KeyValue(questionerCreep.opacityProperty(), 0.2, Interpolator.EASE_BOTH)
                                    )
                            );
                            fadeIn.play();
                        });
                        pauseBeforeFade.play();
                    }

                    if (attempts == 0) {
                        Image questionerImage = new Image("/images/QuestionerJumpscare.gif");
                        questionerJumpscare.setImage(questionerImage);
                        questionerJumpscare.setOpacity(0.2); // Start from 0.2
                        questionerJumpscare.setVisible(true);

                        AudioClip staticSfx = new AudioClip(getClass().getResource("/sounds/static_sfx.mp3").toExternalForm());
                        staticSfx.play();

                        // Animate opacity from 0.2 to 0.9
                        background.setVisible(false);
                        questionerCreep.setVisible(false);
                        Timeline fadeIn = new Timeline(
                                new KeyFrame(Duration.millis(2000), // Duration of fade in ms
                                        new KeyValue(questionerJumpscare.opacityProperty(), 0.9, Interpolator.LINEAR),
                                        new KeyValue(staticGIF.opacityProperty(), 0.15, Interpolator.LINEAR),
                                        new KeyValue(player2_Panel.opacityProperty(), 0.25, Interpolator.LINEAR),
                                        new KeyValue(player3_Panel.opacityProperty(), 0.25, Interpolator.LINEAR),
                                        new KeyValue(player4_Panel.opacityProperty(), 0.25, Interpolator.LINEAR),
                                        new KeyValue(player5_Panel.opacityProperty(), 0.25, Interpolator.LINEAR),
                                        new KeyValue(keyboardVBox.opacityProperty(), 0.25, Interpolator.LINEAR),
                                        new KeyValue(hiddenWord.opacityProperty(), 0.25, Interpolator.LINEAR),
                                        new KeyValue(player1_Health.opacityProperty(), 0.25, Interpolator.LINEAR),
                                        new KeyValue(player1_Char.opacityProperty(), 0.25, Interpolator.LINEAR),
                                        new KeyValue(doorPhase.opacityProperty(), 0.25, Interpolator.LINEAR)
                                )
                        );
                        fadeIn.play();

                        Media sfx = new Media(getClass().getResource("/sounds/jumpscare_sfx.mp3").toExternalForm());
                        MediaPlayer sfxPlayer = new MediaPlayer(sfx);


                        PauseTransition delay = new PauseTransition(Duration.seconds(2));
                        delay.setOnFinished(evt -> sfxPlayer.play());
                        delay.play();
                    }

                    if (is != null) {
                        doorPhase.setImage(new Image(is));
                    } else {
                        System.err.println("Door image not found: " + imagePath);
                        doorPhase.setImage(null);
                    }
                } catch (Exception e) {
                    System.err.println("Failed to load door image: " + imagePath);
                    e.printStackTrace();
                }
                break;
            }
        }
    }

    private void setupCurrentUserPanel(GameInfo gameInfo, String currentUsername) {
        if (gameInfo != null && gameInfo.players != null) {
            // Find current user in player list
            for (PlayerGameState player : gameInfo.players) {
                if (player.userName.equals(currentUsername)) {

                    String characterImagePath = String.format("/char_images/%s_Animation.gif",
                            player.selectedCharacter);
                    try {
                        InputStream is = getClass().getResourceAsStream(characterImagePath);
                        if (is != null) {
                            player1_Char.setImage(new Image(is));
                        } else {

                            player1_Char.setImage(new Image(
                                    getClass().getResourceAsStream("/char_images/default_Animation.gif")));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    int healthPercentage = player.remainingAttempts;
                    player1_Health.setImage(new Image("/images/HealthBar_Phase" +healthPercentage + ".png" ));



                    break;
                }
            }
        }
    }

    private void resetAllPanels() {
        player1_Char.setImage(null);  // Clear Player 1 image
        player2_Panel.setVisible(false);
        player3_Panel.setVisible(false);
        player4_Panel.setVisible(false);
        player5_Panel.setVisible(false);
    }

    private void setupPanel(Pane panel, ImageView charImage, Label nameLabel,
                            ImageView healthImage, PlayerGameState player) {
        panel.setVisible(true);

        // Set character image based on selectedCharacter
        String characterImagePath = String.format("/char_images/%s_Animation.gif",
                player.selectedCharacter);
        charImage.setImage(new Image(characterImagePath));

        // Set player name
        nameLabel.setText(player.userName);
        nameLabel.setFont(Font.font("Yoster Island Regular", FontWeight.NORMAL, 20));
        nameLabel.setTextFill(Color.WHITE);

        // Update health based on remaining attempts (5 is max)
        int healthPercentage = player.remainingAttempts;
        healthImage.setImage(new Image("/images/HealthBar_Phase" +healthPercentage + ".png" ));

        // Additional styling for ready state

        //if (player.isReady) {
        //            panel.setStyle("-fx-effect: dropshadow(gaussian, #00ff00, 10, 0.5, 0, 0);");
        //        } else {
        //            panel.setStyle("");
        //        }


    }


    // </editor-fold>
    public void initializeHiddenWord(GameInfo gameInfo) {
        hiddenWord.getChildren().clear(); // Clear any existing letters

        // Debug output
        System.out.println("Word length: " + (gameInfo != null ? gameInfo.wordLength : "null"));

        if (gameInfo == null || gameInfo.wordLength <= 0) {
            return;
        }

        // Style the HBox container
        hiddenWord.setStyle("-fx-background-color: rgba(50, 50, 50, 0.3); " +  // Semi-transparent dark background
                "-fx-border-radius: 5; " +
                "-fx-padding: 10;");
        hiddenWord.setSpacing(10); // Space between letters
        hiddenWord.setAlignment(Pos.CENTER); // Center the letters

        // Create underscore labels for each letter in the word
        for (int i = 0; i < gameInfo.wordLength; i++) {
            Label letterLabel = new Label("_");

            // Set font and styling
            letterLabel.setFont(Font.font("Monospaced", FontWeight.BOLD, 32)); // Increased font size
            letterLabel.setTextFill(Color.WHITE);

            // Add visible background for each blank
            letterLabel.setStyle("-fx-background-color: rgba(255, 255, 255, 0.1); " +
                    "-fx-background-radius: 3; " +
                    "-fx-padding: 5 15 5 15;"); // Increased padding

            // Set minimum size for consistent appearance
            letterLabel.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);

            hiddenWord.getChildren().add(letterLabel);
        }


        System.out.println("Created " + hiddenWord.getChildren().size() + " letter blanks");
    }

    public void revealLetter(char letter, List<Integer> positions) {
        for (int pos : positions) {
            if (pos >= 0 && pos < hiddenWord.getChildren().size()) {
                Label label = (Label) hiddenWord.getChildren().get(pos);
                label.setText(String.valueOf(letter).toUpperCase());
            }
        }
    }


    public void playWrongGuessAnimation(String keyText) {
        for (HBox row : Arrays.asList(topHBox, midHBox, bottomHBox)) {
            for (Node node : row.getChildren()) {
                if (node instanceof Button && ((Button) node).getText().equalsIgnoreCase(keyText)) {
                    Button keyButton = (Button) node;


                    TranslateTransition shake = new TranslateTransition(Duration.millis(50), row);
                    shake.setFromX(0);
                    shake.setByX(10);
                    shake.setCycleCount(6);
                    shake.setAutoReverse(true);


                    TranslateTransition drop = new TranslateTransition(Duration.millis(500), keyButton);
                    drop.setByY(200);
                    FadeTransition fade = new FadeTransition(Duration.millis(500), keyButton);
                    fade.setToValue(0);
                    ParallelTransition pt = new ParallelTransition(drop, fade);
                    pt.setOnFinished(e -> row.getChildren().remove(keyButton));

                    SequentialTransition seq = new SequentialTransition(shake, pt);
                    seq.play();
                    return;
                }
            }
        }



    }

    public void resetView(){
        roundTimerResult.setVisible(false);
        roundTimerResult.setTextFill(Color.WHITE);
        roundTimerResult.setScaleX(1.0);
        roundTimerResult.setScaleY(1.0);
        questionerJumpscare.setVisible(false);
        background.setVisible(true);
        questionerCreep.setVisible(false);
        doorPhase.setVisible(false);
        doorPhase.setOpacity(1.0);
        staticGIF.setOpacity(0.03);
        Arrays.asList(player2_Panel, player3_Panel, player4_Panel, player5_Panel)
                .forEach(panel -> panel.setOpacity(1.0));
        keyboardVBox.setOpacity(1.0);
        hiddenWord.setOpacity(1.0);

        player1_Char.setOpacity(1.0);
        player1_Health.setOpacity(1.0);
    }

    public void disableResult(){
        roundResult.setVisible(false);
    }

    public Parent getRoot() {
        return root;
    }

    public void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void showMessage(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void setResultWinner(String winnerName, String word, int currentRound) {
        if (roundWinner != null && theWordIs != null && roundWinnerName != null) {
            roundWinner.setImage(new Image("/images/Winner!.gif")); // Ensure the path to your images is correct
            theWordIs.setText("The word was: " + word.toUpperCase());
            roundWinnerName.setText(winnerName + " wins the game!");


            round.setText("ROUND" + currentRound);
            roundResult.setVisible(true);
            roundResult.toFront(); // Ensure the result is on top

            AudioClip winSfx = new AudioClip(getClass().getResource("/sounds/round_win.mp3").toExternalForm());
            winSfx.play();
            // Make sure the result screen doesn't block keyboard interaction
        }
    }

    public void setResultLoser(String word, String winnerName, int currentRound) {

        roundWinner.setImage(new Image("/images/Loser.gif")); // Ensure the path to your images is correct
        theWordIs.setText("The word was: " + word.toUpperCase());
        if (winnerName.equalsIgnoreCase("No Winner")){
            roundWinnerName.setText("No One Guessed the Word");
        }else
            roundWinnerName.setText(winnerName + " wins the game!");

        round.setText("ROUND" + currentRound);
        roundResult.setVisible(true);
        roundResult.toFront();

        AudioClip loseSfx = new AudioClip(getClass().getResource("/sounds/round_lose.mp3").toExternalForm());
        loseSfx.play();

    }

    public void setGameWinner(String winnerName) {
        if (winnerName != null) {
            confetti.setImage(new Image("/images/Confetti.gif"));
            confetti.setVisible(true);
            gameWinner.setImage(new Image("/images/GameWinner!.gif"));
            gameWinner.setVisible(true);

            gameWinnerName.setVisible(true);
            gameWinnerName.setText("Round Winner: " + winnerName +"!");
            gameEnd.setVisible(true);
            gameEnd.toFront();

            AudioClip endSfx = new AudioClip(getClass().getResource("/sounds/game_end.mp3").toExternalForm());
            endSfx.play();
        }
    }

    public void playDamageFlash() {

        damageLabel.setDisable(false);
        damageLabel.setOpacity(0);
        damageLabel.setVisible(true);


        FadeTransition fadeIn = new FadeTransition(Duration.millis(150), damageLabel);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(0.08);


        PauseTransition hold = new PauseTransition(Duration.millis(150));


        FadeTransition fadeOut = new FadeTransition(Duration.millis(150), damageLabel);
        fadeOut.setFromValue(0.08);
        fadeOut.setToValue(0);


        SequentialTransition seq = new SequentialTransition(fadeIn, hold, fadeOut);
        seq.setOnFinished(evt -> damageLabel.setDisable(true));
        seq.play();
    }

}