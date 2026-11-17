package client.player.view;

import client.utility.SceneManager;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.AudioClip;

import java.io.IOException;

public class PlayerStartView {
    private final Parent root;

    @FXML private Button createGame;
    @FXML private ImageView createImage;
    @FXML private Button joinGame;
    @FXML private ImageView joinImage;
    @FXML private Button returnButton;
    @FXML private ImageView returnImage;

    private AudioClip buttonClickedSfx;
    private AudioClip buttonHoverSfx;

    public PlayerStartView() {
        try {
            FXMLLoader fxml = new FXMLLoader(getClass().getResource("/PlayerStart.fxml"));
            fxml.setController(this);
            root = fxml.load();
            SceneManager.addScreen("PlayerStart", root);

            try {
                buttonHoverSfx = new AudioClip(getClass().getResource("/sounds/buttonHover.wav").toExternalForm());
                buttonClickedSfx = new AudioClip(getClass().getResource("/sounds/buttonClicked.wav").toExternalForm());
            } catch (Exception ex) {
                System.err.println("Failed to load button sound effects!");
            }
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

    public void setJoinHandler(EventHandler<ActionEvent> handler) {
        // Hover effect
        joinGame.setOnMouseEntered(e -> {
            if (buttonHoverSfx != null) {
                buttonHoverSfx.stop();
                buttonHoverSfx.play();
            }
            joinImage.setImage(new Image("/images/JoinGame_Hover.png"));
        });
        joinGame.setOnMouseExited(e ->
                joinImage.setImage(new Image("/images/JoinGame_Base.png"))
        );

        // Click effect
        joinGame.setOnAction(event -> {
            if (buttonClickedSfx != null) {
                buttonClickedSfx.stop();
                buttonClickedSfx.play();
            }

            if (handler != null) {
                handler.handle(event); // Run original action
            }
        });
    }

    public void setCreateHandler(EventHandler<ActionEvent> handler) {
        // Hover effect
        createGame.setOnMouseEntered(e -> {
            if (buttonHoverSfx != null) {
                buttonHoverSfx.stop();
                buttonHoverSfx.play();
            }
            createImage.setImage(new Image("/images/CreateGame_Hover.png"));
        });
        createGame.setOnMouseExited(e ->
                createImage.setImage(new Image("/images/CreateGame_Base.png"))
        );

        // Click effect
        createGame.setOnAction(event -> {
            if (buttonClickedSfx != null) {
                buttonClickedSfx.stop();
                buttonClickedSfx.play();
            }

            if (handler != null) {
                handler.handle(event); // Run original action
            }
        });
    }


}
