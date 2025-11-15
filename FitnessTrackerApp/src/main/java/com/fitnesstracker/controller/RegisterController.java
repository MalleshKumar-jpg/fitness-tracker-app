package com.fitnesstracker.controller;

import com.fitnesstracker.App;
import com.fitnesstracker.dao.ActivityDAO;
import com.fitnesstracker.model.Measurement;
import com.fitnesstracker.model.User;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;

/**
 * Handles user registration logic.
 */
public class RegisterController {

    @FXML
    private TextField registerNameField;
    @FXML
    private PasswordField registerPasswordField;
    @FXML
    private PasswordField regConfirmPasswordField;
    @FXML
    private ComboBox<String> genderComboBox;
    @FXML
    private TextField ageField;
    @FXML
    private Label regstatusLabel;

    private final ActivityDAO activityDAO = new ActivityDAO();

    @FXML
    public void initialize() {
        // Populate gender dropdown with options
        genderComboBox.setItems(FXCollections.observableArrayList("Male", "Female", "Other"));
    }

    @FXML
    private void handleRegister() {
        String username = registerNameField.getText().trim();
        String ageText = ageField.getText().trim();
        String gender = genderComboBox.getValue();
        String password = registerPasswordField.getText();
        String confirmPassword = regConfirmPasswordField.getText();

        regstatusLabel.setText("");

        // Validation
        if (username.isEmpty() || password.isEmpty() ||  ageText.isEmpty() || gender == null) {
            regstatusLabel.setText("Username, password, age, and gender are required.");
            return;
        }

        if (activityDAO.nameExists(username)) {
            regstatusLabel.setText("Username already registered. Try logging in.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            regstatusLabel.setText("Passwords do not match.");
            return;
        }

        // Parse and validate age
        int age;
        try {
            age = Integer.parseInt(ageText);
            if (age <= 0 || age > 150) {
                regstatusLabel.setText("Please enter a valid age between 1 and 150.");
                return;
            }
        } catch (NumberFormatException e) {
            regstatusLabel.setText("Age must be a valid number.");
            return;
        }

        // Create and save user
        User newUser = new User();
        newUser.setName(username);
        newUser.setAge(age);
        newUser.setGender(gender);
        newUser.setPassword(password); // NOTE: Password should be hashed in production

        try {
            activityDAO.save(newUser);

            regstatusLabel.setTextFill(javafx.scene.paint.Color.GREEN);
            regstatusLabel.setText("Registration successful! Logging you in...");

            // Automatically log in the new user
            App.showDashboard(newUser);
        } catch (Exception e) {
            regstatusLabel.setText("Registration failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBackToLogin() {
        try {
            App.setRoot("login");
        } catch (Exception e) {
            regstatusLabel.setText("Failed to return to login page.");
            e.printStackTrace();
        }
    }
}