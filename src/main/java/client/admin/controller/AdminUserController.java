package client.admin.controller;

import client.admin.model.AdminUser;
import client.admin.view.AdminUserView;
import client.utility.SceneManager;
import javafx.event.ActionEvent;
import AdminService.Admin;
import CommonTypes.AuthResult;
import CommonTypes.UserInfo;

public class AdminUserController {
    private AdminUserView view;
    private AdminUser model;

    public AdminUserController(Admin adminStub, AuthResult user) {
        model = new AdminUser(adminStub, user);
        view = new AdminUserView();
        initialize();
    }

    public void initialize() {
        loadAllUsers();
        setupHandlers();
    }

    private void loadAllUsers() {
        try {
            UserInfo[] users = model.getAllUsers();
            view.displayUsers(users);
        } catch (Exception e) {
            view.showError("Error", "Failed to load users: " + e.getMessage());
        }
    }

    private void setupHandlers() {
        view.setUserHandler(this::handleUserButton);
        view.setSettingsHandler(this::handleSettingsButton);
        view.setManageHandler(this::handleManageButton);
        view.setSearchHandler(this::handleSearch);
    }

    private void handleUserButton(ActionEvent event) {
        SceneManager.showScreen("AdminUser");
    }

    private void handleSettingsButton(ActionEvent event) {
        SceneManager.showScreen("AdminSettings");
    }

    private void handleManageButton(ActionEvent event) {
            SceneManager.showScreen("AdminUserDetails");
    }

    private void handleSearch(ActionEvent event) {
        String query = view.getSearchQuery();
        if (query == null || query.isEmpty()) {
            loadAllUsers();
        } else {
            UserInfo[] results = model.searchUsers(query);
            view.displayUsers(results);
        }
    }

    public AdminUserView getView() {
        return view;
    }
}