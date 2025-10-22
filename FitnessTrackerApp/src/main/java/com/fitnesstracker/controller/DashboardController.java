package com.fitnesstracker.controller;

import com.fitnesstracker.App;
import com.fitnesstracker.dao.ActivityDAO;
import com.fitnesstracker.model.Measurement;
import com.fitnesstracker.model.Workout;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for the main dashboard with tables for workout and measurement logs.
 * Sorts by ID only (simpler and more reliable since IDs are sequential).
 */
public class DashboardController {

    // --- FXML UI Elements ---
    @FXML private Text welcomeMessageText;
    @FXML private Label totalWorkoutsLabel;
    @FXML private Label totalCaloriesLabel;
    @FXML private Label lastWeightLabel;
    @FXML private Label statusLabel;

    // --- FXML Chart Elements ---
    @FXML private LineChart<String, Number> weightBmiChart;
    @FXML private BarChart<String, Number> calorieBurnChart;

    // --- FXML Table Elements for Measurements (5 columns) ---
    @FXML private TableView<Measurement> measurementTable;
    @FXML private TableColumn<Measurement, String> measurementDateColumn;
    @FXML private TableColumn<Measurement, Double> weightColumn;
    @FXML private TableColumn<Measurement, Double> heightColumn;
    @FXML private TableColumn<Measurement, Double> bodyFatColumn;
    @FXML private TableColumn<Measurement, Double> bmiColumn;

    // --- FXML Table Elements for Workouts (4 columns) ---
    @FXML private TableView<Workout> workoutTable;
    @FXML private TableColumn<Workout, String> workoutDateColumn;
    @FXML private TableColumn<Workout, String> workoutTypeColumn;
    @FXML private TableColumn<Workout, Long> durationColumn;
    @FXML private TableColumn<Workout, Integer> caloriesColumn;

    private final ActivityDAO activityDAO = new ActivityDAO();
    private final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    @FXML
    public void initialize() {
        if (App.getCurrentUser() != null) {
            String name = App.getCurrentUser().getName();
            welcomeMessageText.setText("Welcome back, " + name + "!");

            // Setup tables
            setupMeasurementTable();
            setupWorkoutTable();

            // Load data
            loadSummaryData();
        } else {
            try {
                App.logout();
            } catch (IOException e) {
                System.err.println("FATAL: Cannot load login view on invalid session.");
            }
        }
    }

    /**
     * Configure the measurement table columns (5 columns with BMI and Body Fat)
     */
    private void setupMeasurementTable() {
        measurementDateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getRecorddate().format(DATE_FORMATTER))
        );

        weightColumn.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().getWeight()).asObject()
        );

        heightColumn.setCellValueFactory(cellData -> {
            Double height = cellData.getValue().getHeight();
            return new SimpleDoubleProperty(height != null ? height : 0.0).asObject();
        });

        bmiColumn.setCellValueFactory(cellData -> {
            Measurement m = cellData.getValue();
            double bmi = calculateBMI(m);
            return new SimpleDoubleProperty(bmi).asObject();
        });
    }

    /**
     * Configure the workout table columns (4 columns)
     */
    private void setupWorkoutTable() {
        workoutDateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getWorkoutDate().format(DATE_FORMATTER))
        );

        workoutTypeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getWorkoutType())
        );

        durationColumn.setCellValueFactory(cellData ->
                new SimpleLongProperty(cellData.getValue().getDurationMinutes()).asObject()
        );

        caloriesColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getCaloriesBurned()).asObject()
        );
    }

    /**
     * Calculate BMI from a measurement
     */
    private double calculateBMI(Measurement m) {
        if (m.getHeight() == null || m.getHeight() <= 0 || m.getWeight() == null) {
            return 0.0;
        }
        double heightM = m.getHeight() / 100.0; // Convert cm to meters
        double bmi = m.getWeight() / (heightM * heightM);
        return Math.round(bmi * 10.0) / 10.0;
    }

    /**
     * Get BMI status category and color
     */
    private String getBMIStatus(double bmi) {
        if (bmi == 0.0) return "N/A";
        if (bmi < 18.5) return "Underweight";
        if (bmi < 25.0) return "Normal";
        if (bmi < 30.0) return "Overweight";
        return "Obese";
    }

    /**
     * Get color for BMI status
     */
    private String getBMIColor(double bmi) {
        if (bmi == 0.0) return "#607d8b"; // Gray
        if (bmi < 18.5) return "#2196F3"; // Blue - Underweight
        if (bmi < 25.0) return "#4CAF50"; // Green - Normal
        if (bmi < 30.0) return "#FF9800"; // Orange - Overweight
        return "#F44336"; // Red - Obese
    }

    /**
     * Fetches, calculates, and displays all summary data, charts, and tables.
     */
    private void loadSummaryData() {
        if (App.getCurrentUser() == null) return;
        Long userId = App.getCurrentUser().getUserId();

        List<Workout> workouts = activityDAO.findAllWorkoutsByUserId(userId);
        List<Measurement> measurements = activityDAO.findAllMeasurementsByUserId(userId);

        // 1. CLEAR CHARTS FIRST
        weightBmiChart.getData().clear();
        calorieBurnChart.getData().clear();

        // 2. Calculate Summary Metrics
        int totalWorkouts = workouts.size();

        // Calculate calories burned TODAY only
        LocalDate today = LocalDate.now();
        int caloriesToday = workouts.stream()
                .filter(w -> w.getWorkoutDate().equals(today))
                .mapToInt(Workout::getCaloriesBurned)
                .sum();

        if (totalWorkoutsLabel != null) totalWorkoutsLabel.setText(String.valueOf(totalWorkouts));
        if (totalCaloriesLabel != null) totalCaloriesLabel.setText(String.valueOf(caloriesToday));

        // 3. Find Latest Weight and Calculate BMI Status (using highest ID = newest)
        String lastWeight = "N/A";
        double currentBMI = 0.0;

        if (!measurements.isEmpty()) {
            // Sort by ID only - higher ID = newer entry
            measurements.sort(Comparator.comparing(Measurement::getMeasurementId));

            // Last element has highest ID = most recent entry
            Measurement lastMeasurement = measurements.get(measurements.size() - 1);
            lastWeight = String.format("%.1f kg", lastMeasurement.getWeight());
            currentBMI = calculateBMI(lastMeasurement);
        }

        if (lastWeightLabel != null) lastWeightLabel.setText(lastWeight);

        // 4. Update BMI Status Label with color
        if (statusLabel != null) {
            String bmiStatus = getBMIStatus(currentBMI);
            String bmiText = currentBMI > 0
                    ? String.format("%s (%.1f)", bmiStatus, currentBMI)
                    : "N/A";
            statusLabel.setText(bmiText);
            statusLabel.setStyle(String.format(
                    "-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: %s;",
                    getBMIColor(currentBMI)
            ));
        }

        // 5. Populate Weight Chart (sorted by ID for chronological order)
        XYChart.Series<String, Number> weightSeries = new XYChart.Series<>();
        weightSeries.setName("Weight (kg)");

        measurements.stream()
                .sorted(Comparator.comparing(Measurement::getMeasurementId))
                .forEach(m -> {
                    String date = m.getRecorddate().format(DateTimeFormatter.ofPattern("MMM dd"));
                    weightSeries.getData().add(new XYChart.Data<>(date, m.getWeight()));
                });

        if (!weightSeries.getData().isEmpty()) {
            weightBmiChart.getData().add(weightSeries);
        }

        // 6. Populate Calorie Burn Chart
        Map<String, Integer> caloriesByType = workouts.stream()
                .collect(Collectors.groupingBy(
                        Workout::getWorkoutType,
                        Collectors.summingInt(Workout::getCaloriesBurned)
                ));

        XYChart.Series<String, Number> calorieSeries = new XYChart.Series<>();
        calorieSeries.setName("Total Calories Burned");

        caloriesByType.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEach(entry -> {
                    calorieSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
                });

        if (!calorieSeries.getData().isEmpty()) {
            calorieBurnChart.getData().add(calorieSeries);
        }

        // 7. Populate Measurement Table (sorted by ID DESC = newest first)
        List<Measurement> sortedMeasurements = measurements.stream()
                .sorted(Comparator.comparing(Measurement::getMeasurementId).reversed())
                .collect(Collectors.toList());
        measurementTable.setItems(FXCollections.observableArrayList(sortedMeasurements));

        // 8. Populate Workout Table (sorted by ID DESC = newest first)
        List<Workout> sortedWorkouts = workouts.stream()
                .sorted(Comparator.comparing(Workout::getWorkoutId).reversed())
                .collect(Collectors.toList());
        workoutTable.setItems(FXCollections.observableArrayList(sortedWorkouts));
    }

    @FXML
    private void handleLogWorkout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/WorkoutLogView.fxml"));
            Parent root = loader.load();

            Stage logStage = new Stage();
            logStage.setTitle("Log New Workout");
            logStage.setScene(new Scene(root));
            logStage.setOnHidden(e -> loadSummaryData());
            logStage.show();
        } catch (IOException e) {
            System.err.println("Failed to load WorkoutLogView FXML.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogMeasurement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MeasurementLogView.fxml"));
            Parent root = loader.load();

            Stage logStage = new Stage();
            logStage.setTitle("Log New Measurement");
            logStage.setScene(new Scene(root));
            logStage.setOnHidden(e -> loadSummaryData());
            logStage.show();
        } catch (IOException e) {
            System.err.println("Failed to load MeasurementLogView FXML.");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        try {
            App.logout();
        } catch (IOException e) {
            System.err.println("Failed to load login FXML on logout.");
            e.printStackTrace();
        }
    }
}
