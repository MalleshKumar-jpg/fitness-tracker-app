package com.fitnesstracker.controller;

import com.fitnesstracker.App;
import com.fitnesstracker.dao.ActivityDAO;
import com.fitnesstracker.model.Workout;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDate;

/**
 * Controller for logging workout activities
 * Uses ComboBox dropdown to prevent case sensitivity issues
 */
public class WorkoutLogController {

    @FXML private DatePicker workoutDatePicker;
    @FXML private ComboBox<String> workoutTypeComboBox;
    @FXML private TextField durationField;
    @FXML private TextField caloriesField;
    @FXML private Label statusLabel;

    private final ActivityDAO activityDAO = new ActivityDAO();

    @FXML
    public void initialize() {
        // Set default date to today
        workoutDatePicker.setValue(LocalDate.now());

        // Populate workout type dropdown with predefined options
        workoutTypeComboBox.getItems().addAll(
                "Running",
                "Cycling",
                "Swimming",
                "Weight Training",
                "Yoga",
                "Walking",
                "Cardio",
                "HIIT",
                "Basketball",
                "Soccer",
                "Tennis",
                "Boxing",
                "Dancing",
                "Pilates",
                "CrossFit",
                "Stretching",
                "Other"
        );

        // Optional: Set a default value
        // workoutTypeComboBox.setValue("Running");
    }

    @FXML
    private void handleSaveWorkout() {
        String workoutType = workoutTypeComboBox.getValue();
        String durationText = durationField.getText().trim();
        String caloriesText = caloriesField.getText().trim();
        LocalDate date = workoutDatePicker.getValue();

        // Validation
        if (date == null) {
            statusLabel.setText("Please select a date.");
            return;
        }

        if (workoutType == null || workoutType.trim().isEmpty()) {
            statusLabel.setText("Please select a workout type.");
            return;
        }

        if (durationText.isEmpty()) {
            statusLabel.setText("Duration is required.");
            return;
        }

        if (caloriesText.isEmpty()) {
            statusLabel.setText("Calories burned is required.");
            return;
        }

        long duration;
        try {
            duration = Long.parseLong(durationText);
            if (duration <= 0) {
                statusLabel.setText("Duration must be greater than 0.");
                return;
            }
        } catch (NumberFormatException e) {
            statusLabel.setText("Duration must be a valid number.");
            return;
        }

        int calories;
        try {
            calories = Integer.parseInt(caloriesText);
            if (calories < 0) {
                statusLabel.setText("Calories must be 0 or greater.");
                return;
            }
        } catch (NumberFormatException e) {
            statusLabel.setText("Calories must be a valid number.");
            return;
        }

        // Create and save workout
        Workout workout = new Workout();
        workout.setUser(App.getCurrentUser());
        workout.setWorkoutDate(date);
        workout.setWorkoutType(workoutType);  // Already in proper Title Case from dropdown
        workout.setDurationMinutes(duration);
        workout.setCaloriesBurned(calories);

        try {
            System.out.println("=== SAVING WORKOUT ===");
            System.out.println("Date: " + date);
            System.out.println("Type: " + workoutType);
            System.out.println("Duration: " + duration + " minutes");
            System.out.println("Calories: " + calories);

            activityDAO.save(workout);

            System.out.println("✓ Workout saved successfully!");
            statusLabel.setStyle("-fx-text-fill: green;");
            statusLabel.setText("Workout saved successfully!");

            // Close window after brief delay
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
            System.err.println("✗ Failed to save workout!");
            e.printStackTrace();
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Failed to save workout: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) statusLabel.getScene().getWindow();
        stage.close();
    }
}
