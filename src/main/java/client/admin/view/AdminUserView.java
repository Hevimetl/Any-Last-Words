package client.admin.view;

import client.utility.SceneManager;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import CommonTypes.UserInfo;
import java.io.IOException;

public class AdminUserView {
    private final Parent root;

    @FXML private Button userButton;
    @FXML private Button settingsButton;
    @FXML private Button manageButton;
    @FXML private TableView<UserInfo> userTable;
    @FXML private TableColumn<UserInfo, String> userIdColumn;
    @FXML private TableColumn<UserInfo, String> usernameColumn;
    @FXML private TableColumn<UserInfo, String> passwordColumn;
    @FXML private TableColumn<UserInfo, Integer> winsColumn;
    @FXML private TextField searchField;

    public AdminUserView() {
        FXMLLoader fxml = new FXMLLoader(getClass().getResource("/AdminUserMenu.fxml"));
        fxml.setController(this);

        try {
            root = fxml.load();
            initializeColumns();
            SceneManager.addScreen("AdminUser", root);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load AdminUserMenu.fxml", e);
        } catch (NullPointerException e) {
            throw new RuntimeException("FXML element not found. Check FXML file IDs match Java fields", e);
        }
    }

    private void initializeColumns() {
        // Initialize with null checks
        if (userIdColumn == null || usernameColumn == null ||
                passwordColumn == null || winsColumn == null) {
            throw new RuntimeException("One or more table columns are missing in FXML");
        }

        userIdColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(getSafeString(cellData.getValue().userId)));

        usernameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(getSafeString(cellData.getValue().username)));

        passwordColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(getSafeString(cellData.getValue().password)));

        winsColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(getSafeWins(cellData.getValue())).asObject());
    }

    private String getSafeString(String value) {
        return value != null ? value : "";
    }

    private int getSafeWins(UserInfo user) {
        return (user != null && user.winCount >= 0) ? (int) user.winCount : 0;
    }

    public TableView<UserInfo> getUserTable() {
        return userTable;
    }

    public Parent getRoot() {
        return root;
    }

    public void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void setUserHandler(EventHandler<ActionEvent> handler) {
        userButton.setOnAction(handler);
    }

    public void setSettingsHandler(EventHandler<ActionEvent> handler) {
        settingsButton.setOnAction(handler);
    }

    public void setManageHandler(EventHandler<ActionEvent> handler) {
        manageButton.setOnAction(handler);
    }

    public void setSearchHandler(EventHandler<ActionEvent> handler) {
        searchField.setOnAction(handler);
    }

    public String getSearchQuery() {
        return searchField.getText() != null ? searchField.getText() : "";
    }

    public void displayUsers(UserInfo[] users) {
        if (userTable == null) {
            throw new IllegalStateException("User table not initialized");
        }

        ObservableList<UserInfo> userList = FXCollections.observableArrayList(users);
        userTable.setItems(userList);
        userTable.refresh();
    }
}