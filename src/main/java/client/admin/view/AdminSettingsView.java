package client.admin.view;

import client.utility.SceneManager;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import java.io.IOException;

public class AdminSettingsView {
    private final Parent root;

    @FXML private Button userButton;
    @FXML private Button settingsButton;
    @FXML private Button manageButton;
    @FXML private Button Reset;
    @FXML private Button Update;
    @FXML private Spinner<Integer> wait_time_spinner;
    @FXML private Spinner<Integer> Duration_Spinner;

    public AdminSettingsView() {
        try {
            FXMLLoader fxml = new FXMLLoader(getClass().getResource("/AdminSettings.fxml"));
            fxml.setController(this);
            root = fxml.load();
            SceneManager.addScreen("AdminSettings", root);
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

    public void showSuccess(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Navigation handlers
    public void setUserHandler(EventHandler<ActionEvent> handler) {
        userButton.setOnAction(handler);
    }

    public void setSettingsHandler(EventHandler<ActionEvent> handler) {
        settingsButton.setOnAction(handler);
    }

    public void setManageHandler(EventHandler<ActionEvent> handler) {
        manageButton.setOnAction(handler);
    }

    // Settings operation handlers
    public Spinner<Integer> getWaitTimeSpinner() {
        return wait_time_spinner;
    }

    public Spinner<Integer> getDurationSpinner() {
        return Duration_Spinner;
    }

    public void setUpdateHandler(EventHandler<ActionEvent> handler) {
        Update.setOnAction(handler);
    }

    public void setResetHandler(EventHandler<ActionEvent> handler) {
        Reset.setOnAction(handler);
    }
}