package client.admin.view;

import client.utility.SceneManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import java.io.IOException;

public class AdminUserManageView {
    private final Parent root;

    @FXML private Button userButton;
    @FXML private Button settingsButton;
    @FXML private Button manageButton;
    @FXML private TextField usernameField;
    @FXML private TextField passwordField;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;
    @FXML private Button backButton;

    public AdminUserManageView() {
        try {
            FXMLLoader fxml = new FXMLLoader(getClass().getResource("/AdminUserManage.fxml"));
            fxml.setController(this);
            root = fxml.load();
            SceneManager.addScreen("AdminUserManage", root);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setUserButtonHandler(EventHandler<ActionEvent> handler) {
        userButton.setOnAction(handler);
    }

    public void setSettingsButtonHandler(EventHandler<ActionEvent> handler) {
        settingsButton.setOnAction(handler);
    }

    public void setManageButtonHandler(EventHandler<ActionEvent> handler) {
        manageButton.setOnAction(handler);
    }

    public void setUserDetails(String username, String password) {
        usernameField.setText(username);
        passwordField.setText(password);
    }

    public void setUpdateButtonHandler(Runnable handler) {
        updateButton.setOnAction(event -> handler.run());
    }

    public void setDeleteButtonHandler(Runnable handler) {
        deleteButton.setOnAction(event -> handler.run());
    }

    public void setBackButtonHandler(Runnable handler) {
        backButton.setOnAction(event -> handler.run());
    }

    public String getUsername() {
        return usernameField.getText();
    }

    public String getPassword() {
        return passwordField.getText();
    }

    public Parent getRoot() {
        return root;
    }

    public void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}