package com.fitnesstracker.controller;

import com.fitnesstracker.App;
import com.fitnesstracker.dao.ActivityDAO;
import com.fitnesstracker.model.Workout;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.time.LocalDate;

/**
 * Controller for logging workout activities
 * With added support for editing existing workouts
 */
public class WorkoutLogController {

    @FXML private DatePicker workoutDatePicker;
    @FXML private ComboBox<String> workoutTypeComboBox;
    @FXML private TextField durationField;
    @FXML private TextField caloriesField;
    @FXML private Label statusLabel;

    private final ActivityDAO activityDAO = new ActivityDAO();
    private Workout existingWorkout; // For edit mode
    private boolean editMode = false;

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

        // Set default workout type
        workoutTypeComboBox.setValue("Running");
    }

    /**
     * Initialize this controller for editing an existing workout
     */
    public void initializeForEdit(Workout workout) {
        this.existingWorkout = workout;
        this.editMode = true;

        // Populate fields with existing data
        workoutDatePicker.setValue(workout.getWorkoutDate());
        workoutTypeComboBox.setValue(workout.getWorkoutType());
        durationField.setText(String.valueOf(workout.getDurationMinutes()));
        caloriesField.setText(String.valueOf(workout.getCaloriesBurned()));
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
            statusLabel.setTextFill(Color.RED);
            return;
        }

        if (workoutType == null || workoutType.trim().isEmpty()) {
            statusLabel.setText("Please select a workout type.");
            statusLabel.setTextFill(Color.RED);
            return;
        }

        if (durationText.isEmpty()) {
            statusLabel.setText("Duration is required.");
            statusLabel.setTextFill(Color.RED);
            return;
        }

        // Parse duration
        int duration;
        try {
            duration = Integer.parseInt(durationText);
            if (duration <= 0) {
                statusLabel.setText("Duration must be greater than 0.");
                statusLabel.setTextFill(Color.RED);
                return;
            }
        } catch (NumberFormatException e) {
            statusLabel.setText("Duration must be a valid number.");
            statusLabel.setTextFill(Color.RED);
            return;
        }

        // Parse calories (OPTIONAL - trigger will auto-calculate if empty)
        Integer calories = null;
        if (!caloriesText.isEmpty()) {
            try {
                calories = Integer.parseInt(caloriesText);
                if (calories < 0) {
                    statusLabel.setText("Calories must be 0 or greater.");
                    statusLabel.setTextFill(Color.RED);
                    return;
                }
            } catch (NumberFormatException e) {
                statusLabel.setText("Calories must be a valid number.");
                statusLabel.setTextFill(Color.RED);
                return;
            }
        }

        try {
            // Different handling for edit vs. new workout
            if (editMode && existingWorkout != null) {
                // Update existing workout
                existingWorkout.setWorkoutDate(date);
                existingWorkout.setWorkoutType(workoutType);
                existingWorkout.setDurationMinutes(duration);
                existingWorkout.setCaloriesBurned(calories);

                activityDAO.update(existingWorkout);
                System.out.println("✓ Workout updated successfully!");
                statusLabel.setTextFill(Color.GREEN);
                statusLabel.setText("Workout updated successfully!");
            } else {
                // Create and save new workout
                Workout workout = new Workout();
                workout.setUser(App.getCurrentUser());
                workout.setWorkoutDate(date);
                workout.setWorkoutType(workoutType);
                workout.setDurationMinutes(duration);
                workout.setCaloriesBurned(calories);  // Can be NULL - trigger will calculate

                activityDAO.save(workout);
                System.out.println("✓ Workout saved successfully!");
                statusLabel.setTextFill(Color.GREEN);
                statusLabel.setText("Workout saved successfully!");
            }

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
            statusLabel.setTextFill(Color.RED);

            // Extract specific error messages from triggers
            String errorMessage = e.getMessage();

            if (errorMessage.contains("ORA-20001")) {
                statusLabel.setText("Duration must be greater than 0 minutes");
            } else if (errorMessage.contains("ORA-20002")) {
                statusLabel.setText("Duration cannot exceed 600 minutes (10 hours)");
            } else if (errorMessage.contains("ORA-20003")) {
                statusLabel.setText("Duration is required");
            } else if (errorMessage.contains("ORA-20004")) {
                statusLabel.setText("Calories cannot be negative");
            } else if (errorMessage.contains("ORA-20005")) {
                statusLabel.setText("Calories seems unrealistic (over 5000)");
            } else if (errorMessage.contains("ORA-20006")) {
                statusLabel.setText("Workout date cannot be in the future");
            } else if (errorMessage.contains("ORA-20007")) {
                statusLabel.setText("Workout type is required");
            } else {
                statusLabel.setText("Failed to save: " + extractOracleError(errorMessage));
            }
        }
    }

    /**
     * Helper method to extract clean error message
     */
    private String extractOracleError(String fullError) {
        if (fullError.contains("ORA-")) {
            int startIndex = fullError.indexOf("ORA-");
            int endIndex = fullError.indexOf("\n", startIndex);
            if (endIndex == -1) endIndex = fullError.length();
            return fullError.substring(startIndex, Math.min(endIndex, startIndex + 100));
        }
        return fullError.length() > 100 ? fullError.substring(0, 100) + "..." : fullError;
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) statusLabel.getScene().getWindow();
        stage.close();
    }
}