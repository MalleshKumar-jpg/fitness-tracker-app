package com.fitnesstracker.controller;

import com.fitnesstracker.App;
import com.fitnesstracker.dao.ActivityDAO;
import com.fitnesstracker.model.Measurement;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.time.LocalDate;

/**
 * Controller for logging body measurements
 * With added support for editing existing measurements
 */
public class MeasurementLogController {

    @FXML private DatePicker measurementDatePicker;
    @FXML private TextField weightField;
    @FXML private TextField heightField;
    @FXML private Label statusLabel;
    @FXML private Label bmiResultLabel;

    private final ActivityDAO activityDAO = new ActivityDAO();
    private Measurement existingMeasurement; // For edit mode
    private boolean editMode = false;

    @FXML
    public void initialize() {
        // Set default date to today
        measurementDatePicker.setValue(LocalDate.now());

        // Add listener to automatically calculate BMI when weight or height changes
        weightField.textProperty().addListener((observable, oldValue, newValue) -> calculateAndDisplayBMI());
        heightField.textProperty().addListener((observable, oldValue, newValue) -> calculateAndDisplayBMI());
    }

    /**
     * Initialize this controller for editing an existing measurement
     */
    public void initializeForEdit(Measurement measurement) {
        this.existingMeasurement = measurement;
        this.editMode = true;

        // Populate fields with existing data
        measurementDatePicker.setValue(measurement.getRecorddate());
        weightField.setText(String.valueOf(measurement.getWeight()));

        if (measurement.getHeight() != null) {
            heightField.setText(String.valueOf(measurement.getHeight()));
        }

        // Calculate and display BMI
        calculateAndDisplayBMI();
    }

    private void calculateAndDisplayBMI() {
        try {
            String weightText = weightField.getText().trim();
            String heightText = heightField.getText().trim();

            if (!weightText.isEmpty() && !heightText.isEmpty()) {
                double weight = Double.parseDouble(weightText);
                double height = Double.parseDouble(heightText);

                // Height must be in centimeters, convert to meters for BMI calculation
                double heightMeters = height / 100.0;
                double bmi = weight / (heightMeters * heightMeters);

                // Display formatted BMI with category
                String bmiCategory = getBMICategory(bmi);
                if (bmiResultLabel != null) {
                    bmiResultLabel.setText(String.format("BMI: %.1f (%s)", bmi, bmiCategory));
                    bmiResultLabel.setVisible(true);
                }
            } else {
                if (bmiResultLabel != null) {
                    bmiResultLabel.setVisible(false);
                }
            }
        } catch (NumberFormatException e) {
            if (bmiResultLabel != null) {
                bmiResultLabel.setVisible(false);
            }
        }
    }

    private String getBMICategory(double bmi) {
        if (bmi < 18.5) return "Underweight";
        if (bmi < 25) return "Normal";
        if (bmi < 30) return "Overweight";
        return "Obese";
    }

    @FXML
    private void handleSaveMeasurement() {
        String weightText = weightField.getText().trim();
        String heightText = heightField.getText().trim();
        LocalDate date = measurementDatePicker.getValue();

        // Validation
        if (date == null) {
            statusLabel.setText("Please select a date.");
            statusLabel.setTextFill(Color.RED);
            return;
        }

        if (weightText.isEmpty()) {
            statusLabel.setText("Weight is required.");
            statusLabel.setTextFill(Color.RED);
            return;
        }

        double weight;
        try {
            weight = Double.parseDouble(weightText);
            if (weight <= 0 || weight > 500) {
                statusLabel.setText("Please enter a valid weight (0-500 kg).");
                statusLabel.setTextFill(Color.RED);
                return;
            }
        } catch (NumberFormatException e) {
            statusLabel.setText("Weight must be a valid number.");
            statusLabel.setTextFill(Color.RED);
            return;
        }

        // Height is optional but validate if provided
        Double height = null;
        if (!heightText.isEmpty()) {
            try {
                height = Double.parseDouble(heightText);
                if (height <= 0 || height > 300) {
                    statusLabel.setText("Please enter a valid height (0-300 cm).");
                    statusLabel.setTextFill(Color.RED);
                    return;
                }
            } catch (NumberFormatException e) {
                statusLabel.setText("Height must be a valid number.");
                statusLabel.setTextFill(Color.RED);
                return;
            }
        }

        try {
            // Different handling for edit vs. new measurement
            if (editMode && existingMeasurement != null) {
                // Update existing measurement
                existingMeasurement.setRecorddate(date);
                existingMeasurement.setWeight(weight);
                existingMeasurement.setHeight(height);

                activityDAO.update(existingMeasurement);
                System.out.println("✓ Measurement updated successfully!");
                statusLabel.setTextFill(Color.GREEN);
                statusLabel.setText("Measurement updated successfully!");
            } else {
                // Create and save new measurement
                Measurement measurement = new Measurement();
                measurement.setUser(App.getCurrentUser());
                measurement.setRecorddate(date);
                measurement.setWeight(weight);
                measurement.setHeight(height);

                activityDAO.save(measurement);
                System.out.println("✓ Measurement saved successfully!");
                statusLabel.setTextFill(Color.GREEN);
                statusLabel.setText("Measurement saved successfully!");
            }

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
            statusLabel.setTextFill(Color.RED);
            statusLabel.setText("Failed to save measurement: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) statusLabel.getScene().getWindow();
        stage.close();
    }
}