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

public class PlayerCharactersView {

    private final Parent root;
    @FXML private Button returnButton;
    @FXML private ImageView returnImage;
    @FXML private Button omoriButton;
    @FXML private Button bangsButton;
    @FXML private Button gothButton;
    @FXML private Button smirkButton;
    @FXML private Button ruggedButton;
    @FXML private Button steveButton;
    @FXML private Button amogusButton;
    @FXML private Button makilButton;

    private AudioClip amongUs;
    private AudioClip chickenJockey;
    private AudioClip buttonClickedSfx;
    private AudioClip buttonHoverSfx;

    public PlayerCharactersView() {
        try {
            FXMLLoader fxml = new FXMLLoader(getClass().getResource("/PlayerCharacters.fxml"));
            fxml.setController(this);
            root = fxml.load();
            SceneManager.addScreen("PlayerCharacters", root);

            try {
                buttonHoverSfx = new AudioClip(getClass().getResource("/sounds/buttonHover.wav").toExternalForm());
                buttonClickedSfx = new AudioClip(getClass().getResource("/sounds/buttonClicked.wav").toExternalForm());
            } catch (Exception ex) {
                System.err.println("Failed to load button sound effects!");
            }

            //return Button
            returnButton.setOnMouseEntered (e -> returnImage.setImage(new Image("/images/Return_Hover.png")));
            returnButton.setOnMouseExited (e -> returnImage.setImage(new Image ("/images/Return_Base.png")));

            chickenJockey = new AudioClip(
                    getClass().getResource("/sounds/chicken-jockey.mp3").toExternalForm()
            );

            amongUs = new AudioClip(
                    getClass().getResource("/sounds/AmongUs.mp3").toExternalForm()
            );

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

    public void setOmoriHandler(EventHandler<ActionEvent> handler) {
        omoriButton.setOnAction(handler);
    }

    public void setBangsHandler(EventHandler<ActionEvent> handler) {
        bangsButton.setOnAction(handler);
    }

    public void setGothHandler(EventHandler<ActionEvent> handler) {
        gothButton.setOnAction(handler);
    }

    public void setSmirkHandler(EventHandler<ActionEvent> handler) { smirkButton.setOnAction(handler);}

    public void setRuggedHandler(EventHandler<ActionEvent> handler) { ruggedButton.setOnAction(handler);}

    public void setSteveHandler(EventHandler<ActionEvent> handler) {
        steveButton.setOnAction(event -> {
            if (chickenJockey != null) {
                chickenJockey.play(); // Play the sound
            }

            if (handler != null) {
                handler.handle(event); // Run original logic (e.g., changeCharacter)
            }
        });
    }

    public void setAmogusHandler(EventHandler<ActionEvent> handler) {
        amogusButton.setOnAction(event -> {
            if (amongUs != null) {
                amongUs.play(); // Play the sound
            }

            if (handler != null) {
                handler.handle(event); // Run original logic (e.g., changeCharacter)
            }
        });
    }

    public void setMakilHandler(EventHandler<ActionEvent> handler) { makilButton.setOnAction(handler);}

    public void showMessage(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }



}
