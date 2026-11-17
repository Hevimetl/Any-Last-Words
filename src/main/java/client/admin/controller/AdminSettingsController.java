package client.admin.controller;

import client.admin.model.AdminSettings;
import client.admin.view.AdminSettingsView;
import client.utility.SceneManager;
import javafx.event.ActionEvent;
import AdminService.Admin;
import CommonTypes.AuthResult;
import CommonTypes.Settings;

public class AdminSettingsController {
    private AdminSettingsView view;
    private AdminSettings model;

    public AdminSettingsController(Admin adminStub, AuthResult user) {
        model = new AdminSettings(adminStub, user);
        view = new AdminSettingsView();
        initialize();
    }

    private void initialize() {
        loadCurrentSettings();
        setupHandlers();
    }

    private void loadCurrentSettings() {
        try {
            Settings currentSettings = model.getAdminStub().getTimeSettings(model.getUser().token);
            view.getWaitTimeSpinner().getValueFactory().setValue(currentSettings.waitingTime);
            view.getDurationSpinner().getValueFactory().setValue(currentSettings.roundTime);
        } catch (Exception e) {
            view.showError("Error", "Failed to load settings: " + e.getMessage());
        }
    }

    private void setupHandlers() {
        // Navigation handlers
        view.setUserHandler(this::handleUserButton);
        view.setSettingsHandler(this::handleSettingsButton);
        view.setManageHandler(this::handleManageButton);

        // Settings operation handlers
        view.setUpdateHandler(this::handleUpdate);
        view.setResetHandler(this::handleReset);
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

    private void handleUpdate(ActionEvent event) {
        try {
            Settings newSettings = new Settings();
            newSettings.waitingTime = view.getWaitTimeSpinner().getValue();
            newSettings.roundTime = view.getDurationSpinner().getValue();

            model.getAdminStub().updateTimeSettings(model.getUser().token, newSettings);
            view.showSuccess("Success", "Settings updated successfully!");
        } catch (Exception e) {
            view.showError("Update Failed", e.getMessage());
        }
    }

    private void handleReset(ActionEvent event) {
        try {
            Settings defaultSettings = new Settings();
            defaultSettings.waitingTime = 10;
            defaultSettings.roundTime = 30;

            model.getAdminStub().updateTimeSettings(model.getUser().token, defaultSettings);
            loadCurrentSettings();
            view.showSuccess("Reset", "Settings reset to default values");
        } catch (Exception e) {
            view.showError("Reset Failed", e.getMessage());
        }
    }

    public AdminSettingsView getView() {
        return view;
    }
}