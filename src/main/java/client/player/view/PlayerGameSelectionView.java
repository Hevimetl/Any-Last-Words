package client.player.view;

import client.utility.SceneManager;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import CommonTypes.GameInfo;
import javafx.scene.media.AudioClip;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

public class PlayerGameSelectionView {
    private Consumer<GameInfo> onGameDoubleClicked;
    private final Parent root;
    @FXML private VBox gamesContainer;
    @FXML private ScrollPane outputPanel;
    @FXML private Label roomLabel;
    @FXML private Button returnButton;
    @FXML private ImageView returnImage;
    @FXML private ScrollPane scrollPane;

    private AudioClip buttonClickedSfx;
    private AudioClip buttonHoverSfx;

    public PlayerGameSelectionView() {
        try {
            FXMLLoader fxml = new FXMLLoader(getClass().getResource("/PlayerGameSelection.fxml"));
            fxml.setController(this);
            root = fxml.load();

            SceneManager.addScreen("PlayerGameSelection", root);

            try {
                buttonHoverSfx = new AudioClip(getClass().getResource("/sounds/buttonHover.wav").toExternalForm());
                buttonClickedSfx = new AudioClip(getClass().getResource("/sounds/buttonClicked.wav").toExternalForm());
            } catch (Exception ex) {
                System.err.println("Failed to load button sound effects!");
            }

            returnButton.setOnMouseEntered (e -> returnImage.setImage(new Image("/images/Return_Hover.png")));
            returnButton.setOnMouseExited (e -> returnImage.setImage(new Image ("/images/Return_Base.png")));



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

    public void displayGames(List<GameInfo> games) {
        gamesContainer.getChildren().clear();
        gamesContainer.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");

        // Create header row
        HBox header = new HBox(
                createHeaderLabel("Host", 200),
                createHeaderLabel("Players", 150)
        );
        header.setAlignment(Pos.CENTER);
        header.setStyle("-fx-background-color: black; -fx-padding: 10;");
        gamesContainer.getChildren().add(header);

        // Add each game as a row
        for (GameInfo game : games) {
            HBox gameRow = new HBox(
                    createGameLabel(game.hostName, 200),
                    createGameLabel(
                            game.players.length >= 5 ? "FULL" : String.valueOf(game.players.length), 150)
            );
            gameRow.setAlignment(Pos.CENTER);
            gameRow.setPadding(new Insets(10));
            gameRow.setStyle("-fx-border-color: white; -fx-border-width: 1 1 1 1;");

            gameRow.setUserData(game);
            // Make row clickable
            gameRow.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    GameInfo clickedGame = (GameInfo) gameRow.getUserData();
                    onGameDoubleClicked.accept(clickedGame);
                }
            });
            gamesContainer.getChildren().add(gameRow);
        }
    }

    private Label createHeaderLabel(String text, double width) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: white;");
        label.setFont(Font.font("Yoster Island", FontWeight.BOLD, 20));
        label.setPrefWidth(width);
        return label;
    }

    private Label createGameLabel(String text, double width) {
        Label label = new Label(text);
        label.setPrefWidth(width);
        label.setStyle("-fx-text-fill: white;");
        label.setFont(Font.font("Yoster Island", FontWeight.BOLD, 20));
        return label;
    }

    public void setOnGameDoubleClicked(Consumer<GameInfo> handler) {
        this.onGameDoubleClicked = handler;
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
}
