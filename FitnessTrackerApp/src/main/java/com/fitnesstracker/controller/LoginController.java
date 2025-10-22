package com.fitnesstracker.controller;

import com.fitnesstracker.App;
import com.fitnesstracker.dao.ActivityDAO;
import com.fitnesstracker.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;

/**
 * Handles user login logic.
 */
public class LoginController {

    @FXML
    private TextField loginNameField;
    @FXML
    private PasswordField loginPasswordField;
    @FXML
    private Label statusLabel;

    private final ActivityDAO activityDAO = new ActivityDAO();

    @FXML
    private void handleLogin() {
        String name = loginNameField.getText();
        String password = loginPasswordField.getText();

        User user = activityDAO.authenticateUser(name, password);

        if (user != null) {
            try {
                // Navigate to the dashboard and pass the authenticated user
                App.showDashboard(user);
            } catch (Exception e) {
                statusLabel.setText("Login successful, but failed to load dashboard.");
                e.printStackTrace();
            }
        } else {
            statusLabel.setText("Invalid name or password.");
        }
    }

    @FXML
    private void handleShowRegister() {
        try {
            App.setRoot("register");
        } catch (Exception e) {
            statusLabel.setText("Failed to load registration page.");
            e.printStackTrace();
        }
    }
}