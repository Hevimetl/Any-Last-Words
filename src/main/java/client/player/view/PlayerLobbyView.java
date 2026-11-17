package client.player.view;

import client.utility.SceneManager;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import CommonTypes.GameInfo;
import CommonTypes.PlayerGameState;

import java.io.IOException;
import java.util.Optional;

public class PlayerLobbyView {

    private final Parent root;
    @FXML private ToggleButton readyButton;
    @FXML private ImageView readyImage;
    @FXML private Label timer;
    @FXML private VBox playerList;
    @FXML private Button returnButton;
    @FXML private ImageView returnImage;
    @FXML private ScrollPane scrollPane;

    private AudioClip buttonClickedSfx;
    private AudioClip buttonHoverSfx;

    public PlayerLobbyView() {
        try {
            Font.loadFont(getClass().getResourceAsStream("/fonts/BleedingPixels.ttf"), 120);
            FXMLLoader fxml = new FXMLLoader(getClass().getResource("/PlayerLobby.fxml"));
            fxml.setController(this);
            root = fxml.load();
            SceneManager.addScreen("PlayerLobby", root);

            try {
                buttonHoverSfx = new AudioClip(getClass().getResource("/sounds/buttonHover.wav").toExternalForm());
                buttonClickedSfx = new AudioClip(getClass().getResource("/sounds/buttonClicked.wav").toExternalForm());
            } catch (Exception ex) {
                System.err.println("Failed to load button sound effects!");
            }

            readyButton.setOnMouseEntered(e -> {
                try {
                    readyImage.setImage(new Image(getClass().getResource("/images/Ready_Hover.png").toExternalForm()));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            readyButton.setOnMouseExited(e -> {
                try {
                    readyImage.setImage(new Image(getClass().getResource("/images/Ready_Base.png").toExternalForm()));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

    public void setReadyHandler(EventHandler<ActionEvent> handler) {
        // Set click handler with sound
        readyButton.setOnAction(event -> {
            if (buttonClickedSfx != null) {
                buttonClickedSfx.stop();
                buttonClickedSfx.play();
            }

            if (handler != null) {
                handler.handle(event); // Run original logic (e.g., toggle ready status)
            }
        });

        // Set hover handler with sound and image change
        readyButton.setOnMouseEntered(event -> {
            if (buttonHoverSfx != null) {
                buttonHoverSfx.stop();
                buttonHoverSfx.play();
            }

            try {
                readyImage.setImage(new Image(getClass().getResource("/images/Ready_Hover.png").toExternalForm()));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        readyButton.setOnMouseExited(event -> {
            try {
                readyImage.setImage(new Image(getClass().getResource("/images/Ready_Base.png").toExternalForm()));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void updateTimerLabel(int secondsLeft) {
        timer.setText(Integer.toString(secondsLeft));
    }


    public void updatePlayerList(GameInfo currentGame) {
        playerList.getChildren().clear();
        playerList.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");

        // Create header row
        HBox header = new HBox(
                createHeaderLabel("Player", 200),
                createHeaderLabel("Status", 150)
        );
        header.setStyle("-fx-background-color: black; -fx-padding: 10;");
        playerList.getChildren().add(header);

        // Add each player as a row
        for (PlayerGameState playerState : currentGame.players) {
            HBox playerRow = new HBox(
                    createPlayerLabel(playerState.userName, 200),
                    createStatusLabel(playerState.isReady, 150)
            );

            playerRow.setPadding(new Insets(10));
            playerRow.setStyle("-fx-border-color: white; -fx-border-width: 1 1 1 1;");
            playerList.getChildren().add(playerRow);
        }
    }

    private Label createHeaderLabel(String text, double width) {
        Label label = new Label(text);
        label.setPrefWidth(width);
        label.setStyle("-fx-text-fill: white;");
        label.setFont(Font.font("Yoster Island", FontWeight.BOLD, 14));
        return label;
    }

    private Label createPlayerLabel(String text, double width) {
        Label label = new Label(text);
        label.setPrefWidth(width);
        label.setStyle("-fx-text-fill: white;");
        label.setFont(Font.font("Yoster Island", FontWeight.NORMAL, 13));
        return label;
    }

    private Label createStatusLabel(boolean isReady, double width) {
        Label label = new Label(isReady ? "READY" : "NOT READY");
        label.setPrefWidth(width);
        label.setFont(Font.font("Yoster Island", isReady ? FontWeight.BOLD : FontWeight.NORMAL, 13));
        label.setTextFill(isReady ? Color.GREEN : Color.RED);
        return label;
    }

    public void setReturnHandler(EventHandler<ActionEvent> handler) {
        // Set click handler with sound
        returnButton.setOnAction(event -> {
            if (buttonClickedSfx != null) {
                buttonClickedSfx.stop();
                buttonClickedSfx.play();
            }

            if (handler != null) {
                handler.handle(event); // Run original logic (e.g., go back to menu)
            }
        });

        // Set hover handler with sound and image change
        returnButton.setOnMouseEntered(event -> {
            if (buttonHoverSfx != null) {
                buttonHoverSfx.stop();
                buttonHoverSfx.play();
            }
            returnImage.setImage(new Image("/images/Return_Hover.png"));
        });

        returnButton.setOnMouseExited(event -> {
            returnImage.setImage(new Image("/images/Return_Base.png"));
        });
    }


    public boolean showWaitPrompt() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Wait for Players?");
        alert.setHeaderText("Not enough players are ready");
        alert.setContentText("Do you want to wait for another player?");

        ButtonType yesButton = new ButtonType("Yes");
        ButtonType noButton = new ButtonType("No");
        alert.getButtonTypes().setAll(yesButton, noButton);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == yesButton;
    }

}
