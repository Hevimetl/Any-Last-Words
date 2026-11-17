package client.player.view;

import client.utility.SceneManager;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

import javafx.scene.layout.Pane;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;

public class PlayerMenuView {
    private MediaPlayer bgMusicPlayer;

    private final Parent root;
    @FXML private Button leaderboardButton;
    @FXML private ImageView leaderboardImage;
    @FXML private Button startButton;
    @FXML private Button charactersButton;
    @FXML private Button exitButton;
    @FXML private Button tutorialButton;
    @FXML private ImageView tutorialImage;
    @FXML private ImageView buttonImage;
    @FXML private ImageView transitionGIF;
    @FXML private ImageView exitImage;
    @FXML private ImageView charactersImage;
    @FXML private Pane tutorialPane;
    @FXML private ImageView tutorialInstructions;
    @FXML private Button returnButton;
    @FXML private ImageView returnImage;



    private AudioClip doorOpen;
    private AudioClip doorSqeakSfx;
    private AudioClip buttonClickedSfx;
    private AudioClip buttonHoverSfx;

    public PlayerMenuView() {
        try {
            FXMLLoader fxml = new FXMLLoader(getClass().getResource("/PlayerMenu.fxml"));
            fxml.setController(this);
            root = fxml.load();
            SceneManager.addScreen("PlayerMenu", root);
            doorOpen =   new AudioClip(
                    getClass().getResource("/sounds/door_open.wav")
                            .toExternalForm()

            );
            doorOpen.setVolume(0.09);


            //Buttons
            try {
                buttonHoverSfx = new AudioClip(getClass().getResource("/sounds/ButtonHover.wav").toExternalForm());
                buttonClickedSfx = new AudioClip(getClass().getResource("/sounds/ButtonClicked.wav").toExternalForm());

            } catch (Exception ex) {
                System.err.println("Failed to load button sound effects!");
            }

            //Start Button
            startButton.setOnMouseEntered(e -> {
                        buttonImage.setImage(new Image("/images/DoorButton_Hover.png"));
                        AudioClip hoverSound = new AudioClip(
                                getClass().getResource("/sounds/door_hover.mp3")
                                        .toExternalForm()
                        );
                        hoverSound.setVolume(0.5);
                        hoverSound.play();
                    });
            startButton.setOnMouseExited (e -> buttonImage.setImage(new Image ("/images/DoorButton_Base.png")));

            startButton.setOnMouseClicked (e ->{
                transitionGIF.setVisible(true);
            transitionGIF.setImage(new Image ("/images/EnterGame_Base.gif"));
            leaderboardButton.setVisible(false);
            startButton.setVisible(false);
            charactersButton.setVisible(false);
            exitButton.setVisible(false);
            buttonImage.setVisible(false);
            tutorialButton.setVisible(false);

            doorOpen.play();

                PauseTransition pause = new PauseTransition(Duration.seconds(2));
                pause.setOnFinished(event -> setTransitionGIFVisibility());
                pause.play();
            });

            tutorialButton.setOnMouseClicked(event -> {
                if (buttonClickedSfx != null) {
                    buttonClickedSfx.stop();
                    buttonClickedSfx.play();
                }

                // Toggle visibility of the entire pane (includes image)
                boolean isVisible = tutorialPane.isVisible();
                tutorialPane.setVisible(!isVisible);

                if (!isVisible) {
                    tutorialPane.toFront(); // Bring to front only when showing
                }
            });

            tutorialButton.setOnMouseEntered(event -> {
                if (buttonHoverSfx != null) {
                    buttonHoverSfx.stop();
                    buttonHoverSfx.play();
                }
                tutorialImage.setImage(new Image("/images/Tutorial_Hover.png"));
            });

            tutorialButton.setOnMouseExited(event -> {
                tutorialImage.setImage(new Image("/images/Tutorial_Base.png"));
            });

            // Return Button Setup - No controller needed
            returnButton.setOnAction(event -> {
                if (buttonClickedSfx != null) {
                    buttonClickedSfx.stop();
                    buttonClickedSfx.play();
                }

                // Hide tutorial pane
                tutorialPane.setVisible(false);

                // Show all main menu UI elements
                leaderboardButton.setVisible(true);
                startButton.setVisible(true);
                charactersButton.setVisible(true);
                exitButton.setVisible(true);
                tutorialButton.setVisible(true);
                buttonImage.setVisible(true); // e.g., the door image

                // Optional: Change return button image back to base state
                returnImage.setImage(new Image("/images/Return_Base.png"));
            });

// Hover effects for return button
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

    public void setStartHandler(EventHandler<ActionEvent> handler) {
        startButton.setOnAction(handler);
    }

    public void setCharactersHandler(EventHandler<ActionEvent> handler) {
        // Set click handler with sound
        charactersButton.setOnAction(event -> {
            if (buttonClickedSfx != null) {
                buttonClickedSfx.stop();
                buttonClickedSfx.play();
            }
            handler.handle(event);
        });

        // Set hover handler to play sound AND change image
        charactersButton.setOnMouseEntered(event -> {
            // Play hover sound
            if (buttonHoverSfx != null) {
                buttonHoverSfx.stop();
                buttonHoverSfx.play();
            }

            // Change image to hover state
            charactersImage.setImage(new Image("/images/Characters_Hover.png"));
        });

        // Set exit handler to reset image
        charactersButton.setOnMouseExited(event -> {
            charactersImage.setImage(new Image("/images/Characters_Base.png"));
        });
    }

    public void setExitHandler(EventHandler<ActionEvent> handler) {
        exitButton.setOnAction(event -> {
            if (buttonClickedSfx != null) {
                buttonClickedSfx.stop();
                buttonClickedSfx.play();
            }
            handler.handle(event);
        });

        // Set hover handler to play sound AND change image
        exitButton.setOnMouseEntered(event -> {
            // Play hover sound
            if (buttonHoverSfx != null) {
                buttonHoverSfx.stop();
                buttonHoverSfx.play();
            }

            // Change image to hover state
            exitImage.setImage(new Image("/images/Exit_Hover.png"));
        });

        // Set exit handler to reset image
        exitButton.setOnMouseExited(event -> {
            exitImage.setImage(new Image("/images/Exit_Base.png"));
        });
    }


    public void setLeaderboardHandler(EventHandler<ActionEvent> handler) {
        leaderboardButton.setOnAction(event -> {
            if (buttonClickedSfx != null) {
                buttonClickedSfx.stop();
                buttonClickedSfx.play();
            }
            handler.handle(event);
        });

        // Set hover handler to play sound AND change image
        leaderboardButton.setOnMouseEntered(event -> {
            // Play hover sound
            if (buttonHoverSfx != null) {
                buttonHoverSfx.stop();
                buttonHoverSfx.play();
            }

            // Change image to hover state
            leaderboardImage.setImage(new Image("/images/Leaderboard_Hover.png"));
        });

        // Set exit handler to reset image
        leaderboardButton.setOnMouseExited(event -> {
            leaderboardImage.setImage(new Image("/images/Leaderboard_Base.png"));
        });
    }

    public void setTransitionGIFVisibility(){
        leaderboardButton.setVisible(true);
        startButton.setVisible(true);
        charactersButton.setVisible(true);
        exitButton.setVisible(true);
        buttonImage.setVisible(true);
        tutorialButton.setVisible(true);
        transitionGIF.setVisible(false);
    }

    public boolean confirmExit() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Exit");
        confirm.setHeaderText("Are you sure you want to exit?");
        confirm.setContentText("Press OK to exit, or Cancel to stay in the menu.");

        Optional<ButtonType> result = confirm.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }


}
