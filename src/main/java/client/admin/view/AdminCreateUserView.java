package client.admin.view;

import client.utility.SceneManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import java.io.IOException;
import javafx.event.EventHandler;

public class AdminCreateUserView {
    private Parent root;

    @FXML private TextField usernameField;
    @FXML private TextField passwordField;
    @FXML private Button createUserButton;
    @FXML private Button userButton;
    @FXML private Button settingsButton;
    @FXML private Button manageButton;
    @FXML private Button backButton;

    public AdminCreateUserView() {
        try {
            FXMLLoader fxml = new FXMLLoader(getClass().getResource("/AdminCreateUser.fxml"));
            fxml.setController(this);
            root = fxml.load();
            SceneManager.addScreen("AdminCreateUser", root);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

    public void setBackButtonHandler(EventHandler<ActionEvent> handler) {
        backButton.setOnAction(handler);
    }

    // User creation handler
    public void setCreateUserHandler(Runnable handler) {
        createUserButton.setOnAction(e -> handler.run());
    }

    // Getters for UI components
    public String getUsername() {
        return usernameField.getText().trim();
    }

    public String getPassword() {
        return passwordField.getText().trim();
    }

    public void clearFields() {
        usernameField.clear();
        passwordField.clear();
    }
    public void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public Parent getRoot() {
        return root;
    }
}