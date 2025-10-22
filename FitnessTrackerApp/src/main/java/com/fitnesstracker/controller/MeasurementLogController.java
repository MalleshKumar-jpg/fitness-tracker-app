package com.fitnesstracker.controller;

import com.fitnesstracker.App;
import com.fitnesstracker.dao.ActivityDAO;
import com.fitnesstracker.model.Measurement;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDate;

/**
 * Controller for logging body measurements (weight and height only)
 * Body fat percentage removed for simplicity
 */
public class MeasurementLogController {

    @FXML private DatePicker measurementDatePicker;
    @FXML private TextField weightField;
    @FXML private TextField heightField;
    @FXML private Label statusLabel;

    private final ActivityDAO activityDAO = new ActivityDAO();

    @FXML
    public void initialize() {
        // Set default date to today
        measurementDatePicker.setValue(LocalDate.now());
    }

    @FXML
    private void handleSaveMeasurement() {
        String weightText = weightField.getText().trim();
        String heightText = heightField.getText().trim();
        LocalDate date = measurementDatePicker.getValue();

        // Validation
        if (date == null) {
            statusLabel.setText("Please select a date.");
            return;
        }

        if (weightText.isEmpty()) {
            statusLabel.setText("Weight is required.");
            return;
        }

        double weight;
        try {
            weight = Double.parseDouble(weightText);
            if (weight <= 0 || weight > 500) {
                statusLabel.setText("Please enter a valid weight (0-500 kg).");
                return;
            }
        } catch (NumberFormatException e) {
            statusLabel.setText("Weight must be a valid number.");
            return;
        }

        // Height is optional but validate if provided
        Double height = null;
        if (!heightText.isEmpty()) {
            try {
                height = Double.parseDouble(heightText);
                if (height <= 0 || height > 300) {
                    statusLabel.setText("Please enter a valid height (0-300 cm).");
                    return;
                }
            } catch (NumberFormatException e) {
                statusLabel.setText("Height must be a valid number.");
                return;
            }
        }

        // Create and save measurement (without body fat)
        Measurement measurement = new Measurement();
        measurement.setUser(App.getCurrentUser());
        measurement.setRecorddate(date);
        measurement.setWeight(weight);
        measurement.setHeight(height);

        try {
            System.out.println("=== SAVING MEASUREMENT ===");
            System.out.println("Date: " + date);
            System.out.println("Weight: " + weight + " kg");
            System.out.println("Height: " + height + " cm");

            activityDAO.save(measurement);

            System.out.println("✓ Measurement saved successfully!");
            statusLabel.setStyle("-fx-text-fill: green;");
            statusLabel.setText("Measurement saved successfully!");

            // Close the window after a brief delay
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    javafx.application.Platform.runLater(() -> {
                        Stage stage = (Stage) statusLabel.getScene().getWindow();
                        stage.close();
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (Exception e) {
            System.err.println("✗ Failed to save measurement!");
            e.printStackTrace();
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Failed to save measurement: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) statusLabel.getScene().getWindow();
        stage.close();
    }
}