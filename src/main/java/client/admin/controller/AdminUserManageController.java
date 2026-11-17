package client.admin.controller;

import client.admin.model.AdminUserManage;
import client.admin.view.AdminUserManageView;
import client.common.controller.MainController;
import client.utility.SceneManager;
import AdminService.Admin;
import CommonTypes.AuthResult;
import CommonTypes.UserInfo;
import javafx.event.ActionEvent;

public class AdminUserManageController {
    private final AdminUserManage model;
    private final AdminUserManageView view;

    public AdminUserManageController(Admin adminStub, AuthResult user, UserInfo selectedUser) {
        this.model = new AdminUserManage(adminStub, user, selectedUser);
        this.view = new AdminUserManageView();
        initialize();
    }

    private void initialize() {
        UserInfo user = model.getCurrentUser();
        view.setUserDetails(user.username, user.password);

        view.setUserButtonHandler(this::handleUserButton);
        view.setSettingsButtonHandler(this::handleSettingsButton);
        view.setManageButtonHandler(this::handleManageButton);

        view.setUpdateButtonHandler(this::handleUpdate);
        view.setDeleteButtonHandler(this::handleDelete);
        view.setBackButtonHandler(this::handleBack);
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

    private void handleUpdate() {
        try {
            String newUsername = view.getUsername();
            String newPassword = view.getPassword();
            UserInfo currentUser = model.getCurrentUser();

            if (newUsername.equals(currentUser.username) && newPassword.equals(currentUser.password)) {
                newUsername = null; // No change needed
            }

            if (newPassword.isEmpty()) {
                newPassword = null; // No change needed
            }

            if (newUsername == null && newPassword == null) {
                view.showError("No Changes", "Please modify either username or password before updating");
                return;
            }

            model.updateUser(newUsername, newPassword);
            view.showSuccess("User updated successfully");

            // Update view with new values
            if (newUsername != null) {
                currentUser.username = newUsername;
            }
            if (newPassword != null) {
                currentUser.password = newPassword;
            }
            view.setUserDetails(currentUser.username, currentUser.password);

        } catch (Exception e) {
            view.showError("Update Error", "Failed to update user: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleDelete() {
        try {
            model.deleteUser();
            view.showSuccess("User deleted successfully");
            handleBack();
        } catch (Exception e) {
            view.showError("Delete Error", "Failed to delete user: " + e.getMessage());
        }
    }

    private void handleBack() {
        MainController.updateViews();
        SceneManager.switchToScreen("AdminUserDetails");
    }


}