package client.admin.controller;

import client.admin.model.AdminUserDetails;
import client.admin.view.AdminUserDetailsView;
import client.utility.SceneManager;
import AdminService.Admin;
import CommonTypes.AuthResult;
import CommonTypes.UserInfo;
import javafx.event.ActionEvent;

public class AdminUserDetailsController {
    private final AdminUserDetails model;
    private final AdminUserDetailsView view;

    public AdminUserDetailsController(Admin adminStub, AuthResult user) {
        model = new AdminUserDetails(adminStub, user);
        view = new AdminUserDetailsView();
        initialize();
    }

    public void initialize() {
        try {
            view.populateUserGrid(model.getAllUsers());
            view.setDetailsButtonHandler((event, user) -> handleDetails(user));

            // Set up navigation button handlers
            view.setSettingsButtonHandler(this::handleSettingsButton);
            view.setManageButtonHandler(this::handleManageButton);
            view.setAddUserButtonHandler(this::handleAddUserButton);

        } catch (Exception e) {
            view.showError("Loading Error", "Failed to load users: " + e.getMessage());
        }
    }

    // Navigation handlers
    private void handleSettingsButton(ActionEvent event) {
        SceneManager.showScreen("AdminSettings");
    }

    private void handleManageButton(ActionEvent event) {
        SceneManager.showScreen("AdminUserDetails");
    }

    private void handleAddUserButton(ActionEvent event) {
        SceneManager.showScreen("AdminCreateUser");
    }

    private void handleDetails(UserInfo user) {
        try {
            AdminUserManageController adminManageController = new AdminUserManageController(
                    getAdminStub(),
                    getUser(),
                    user
            );
            SceneManager.showScreen("AdminUserManage");
        } catch (Exception e) {
            view.showError("Error", "Failed to open user management: " + e.getMessage());
        }
    }

    public Admin getAdminStub() {
        return model.getAdminStub();
    }

    public AuthResult getUser() {
        return model.getUser();
    }

    public AdminUserDetailsView getView() {
        return view;
    }
}
