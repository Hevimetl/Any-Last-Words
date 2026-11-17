package client.player.view;

import client.utility.SceneManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import server.utility.objects.PlayerData;

import java.io.IOException;

public class PlayerLeaderboardView {
    private final Parent root;

    @FXML private TableView<PlayerData> leaderboardTable;
    @FXML private TableColumn<PlayerData, Integer> userIdColumn;
    @FXML private TableColumn<PlayerData, String> usernameColumn;
    @FXML private TableColumn<PlayerData, Integer> winsColumn;
    @FXML private Button returnButton;
    @FXML private ImageView returnImage;

    private AudioClip buttonClickedSfx;
    private AudioClip buttonHoverSfx;

    public PlayerLeaderboardView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/PlayerLeaderboards.fxml"));
            loader.setController(this);
            root = loader.load();
            SceneManager.addScreen("PlayerLeaderboard", root);

            try {
                buttonHoverSfx = new AudioClip(getClass().getResource("/sounds/buttonHover.wav").toExternalForm());
                buttonClickedSfx = new AudioClip(getClass().getResource("/sounds/buttonClicked.wav").toExternalForm());
            } catch (Exception ex) {
                System.err.println("Failed to load button sound effects!");
            }

            setupCustomTableStyling();
            configureTableColumnResizePolicy();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void setupCustomTableStyling() {
        leaderboardTable.setStyle("-fx-background-color: white;");

        // Apply styling to each column
        configureColumn(userIdColumn);
        configureColumn(usernameColumn);
        configureColumn(winsColumn);

        // Style each row like in the GameSelectionView
        leaderboardTable.setRowFactory(tv -> new TableRow<PlayerData>() {
            @Override
            protected void updateItem(PlayerData item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                    setText(null);
                } else {
                    setStyle("-fx-background-color: black; -fx-border-color: white;");
                }
            }
        });
    }

    private <T> void configureColumn(TableColumn<PlayerData, T> column) {
        column.setCellFactory(col -> new TableCell<PlayerData, T>() {
            private final Label label = new Label();

            {
                label.setFont(Font.font("Yoster Island", FontWeight.BOLD, 15));
                label.setTextFill(Color.WHITE);
                label.setAlignment(Pos.CENTER);
                setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    label.setText(item.toString());
                    setGraphic(label);
                }
            }
        });

        column.setStyle("-fx-background-color: rgba(0,0,0,0.9);");
    }

    private void configureTableColumnResizePolicy() {
        // Make columns fill available space
        leaderboardTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        userIdColumn.setPrefWidth(120);   // UserID
        usernameColumn.setPrefWidth(320); // Username
        winsColumn.setPrefWidth(120);
    }

    public void displayTopPlayers(String[] players) {
        // Set up how each column gets its data
        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        winsColumn.setCellValueFactory(new PropertyValueFactory<>("wins"));

        // Populate the table
        ObservableList<PlayerData> data = FXCollections.observableArrayList();

        for (String player : players) {
            String[] parts = player.split(",");
            if (parts.length == 3) {
                try {
                    int userId = Integer.parseInt(parts[0].trim());
                    String username = parts[1].trim();
                    int wins = Integer.parseInt(parts[2].trim());

                    data.add(new PlayerData(userId, username, wins));
                } catch (NumberFormatException ignored) {}
            }
        }

        leaderboardTable.setItems(data);
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

    public Parent getRoot() {
        return root;
    }
}