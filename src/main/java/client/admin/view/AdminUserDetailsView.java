package client.admin.view;

import client.utility.SceneManager;
import CommonTypes.UserInfo;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.event.ActionEvent;

import java.io.IOException;
import java.util.List;
import java.util.function.BiConsumer;

public class AdminUserDetailsView {
    @FXML private ScrollPane scrollPane;
    @FXML private GridPane userGrid;
    @FXML private Button userButton;
    @FXML private Button settingsButton;
    @FXML private Button manageButton;
    @FXML private Button addUserButton;
    private final Parent root;

    public AdminUserDetailsView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AdminUserDetails.fxml"));
            loader.setController(this);
            root = loader.load();
            SceneManager.addScreen("AdminUserDetails", root);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load FXML: " + e.getMessage(), e);
        }
    }

    public void populateUserGrid(List<UserInfo> users) {
        userGrid.getChildren().clear();
        userGrid.setHgap(20);
        userGrid.setVgap(20);
        userGrid.setPadding(new Insets(15));

        int col = 0, row = 0;
        for (UserInfo user : users) {
            VBox card = createUserCard(user);
            userGrid.add(card, col, row);

            col = (col + 1) % 2;
            if (col == 0) row++;
        }
    }

    private VBox createUserCard(UserInfo user) {
        VBox card = new VBox(10);
        card.getStyleClass().add("user-card");
        card.setPrefSize(300, 120);
        card.setPadding(new Insets(15));

        String info = String.format(
                "ID: %s\nUsername: %s",
                user.userId, user.username
        );

        Label infoLabel = new Label(info);
        infoLabel.getStyleClass().add("user-info");

        Button detailsButton = new Button("Details");
        detailsButton.getStyleClass().add("details-button");
        detailsButton.setUserData(user);

        card.getChildren().addAll(infoLabel, detailsButton);
        return card;
    }

    // Navigation button handlers
    public void setUserButtonHandler(EventHandler<ActionEvent> handler) {
        userButton.setOnAction(handler);
    }

    public void setSettingsButtonHandler(EventHandler<ActionEvent> handler) {
        settingsButton.setOnAction(handler);
    }

    public void setManageButtonHandler(EventHandler<ActionEvent> handler) {
        manageButton.setOnAction(handler);
    }

    public void setAddUserButtonHandler(EventHandler<ActionEvent> handler) {
        addUserButton.setOnAction(handler);
    }

    public void setDetailsButtonHandler(BiConsumer<ActionEvent, UserInfo> handler) {
        userGrid.getChildren().forEach(node -> {
            VBox card = (VBox) node;
            Button btn = (Button) card.getChildren().get(1);
            btn.setOnAction(event -> handler.accept(event, (UserInfo) btn.getUserData()));
        });
    }

    public void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public Parent getRoot() {
        return root;
    }
}