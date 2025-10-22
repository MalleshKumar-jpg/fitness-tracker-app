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
import javafx.collections.ObservableList;
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
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for the main dashboard with charts AND tables
 */
public class DashboardController {

    // UI Elements
    @FXML private Text welcomeMessageText;
    @FXML private Label totalWorkoutsLabel;
    @FXML private Label totalCaloriesLabel;
    @FXML private Label lastWeightLabel;
    @FXML private Label statusLabel;

    // Charts
    @FXML private LineChart<String, Number> weightBmiChart;
    @FXML private BarChart<String, Number> calorieBurnChart;

    // Measurement Table
    @FXML private TableView<Measurement> measurementTable;
    @FXML private TableColumn<Measurement, String> measurementDateColumn;
    @FXML private TableColumn<Measurement, Double> weightColumn;
    @FXML private TableColumn<Measurement, Double> heightColumn;
    @FXML private TableColumn<Measurement, Double> bmiColumn;

    // Workout Table
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
     * Setup measurement table columns
     */
    private void setupMeasurementTable() {
        measurementDateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getRecorddate().format(DATE_FORMATTER)
                )
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
     * Setup workout table columns
     */
    private void setupWorkoutTable() {
        workoutDateColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getWorkoutDate().format(DATE_FORMATTER)
                )
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
     * Calculate BMI from measurement
     */
    private double calculateBMI(Measurement m) {
        if (m.getHeight() == null || m.getHeight() <= 0 || m.getWeight() == null) {
            return 0.0;
        }
        double heightM = m.getHeight() / 100.0;
        double bmi = m.getWeight() / (heightM * heightM);
        return Math.round(bmi * 10.0) / 10.0;
    }

    /**
     * Load all summary data, charts, and tables
     */
    private void loadSummaryData() {
        if (App.getCurrentUser() == null) return;
        Long userId = App.getCurrentUser().getUserId();

        List<Workout> workouts = activityDAO.findAllWorkoutsByUserId(userId);
        List<Measurement> measurements = activityDAO.findAllMeasurementsByUserId(userId);

        System.out.println("DEBUG: Loaded " + workouts.size() + " workouts");
        System.out.println("DEBUG: Loaded " + measurements.size() + " measurements");

        // Clear charts
        weightBmiChart.getData().clear();
        calorieBurnChart.getData().clear();

        // Calculate summary metrics
        int totalWorkouts = workouts.size();
        int totalCalories = workouts.stream()
                .mapToInt(Workout::getCaloriesBurned)
                .sum();

        if (totalWorkoutsLabel != null) totalWorkoutsLabel.setText(String.valueOf(totalWorkouts));
        if (totalCaloriesLabel != null) totalCaloriesLabel.setText(String.valueOf(totalCalories));

        // Find latest weight and BMI
        String lastWeight = "N/A";
        String status = "N/A";
        if (!measurements.isEmpty()) {
            measurements.sort(Comparator.comparing(Measurement::getRecorddate));
            Measurement lastMeasurement = measurements.get(measurements.size() - 1);
            lastWeight = String.format("%.1f kg", lastMeasurement.getWeight());

            double bmi = calculateBMI(lastMeasurement);
            if (bmi > 0) {
                String bmiCategory = getBMICategory(bmi);
                status = String.format("%s (%.1f)", bmiCategory, bmi);
            }
        }
        if (lastWeightLabel != null) lastWeightLabel.setText(lastWeight);
        if (statusLabel != null) statusLabel.setText(status);

        // Populate weight chart
        measurements.sort(Comparator.comparing(Measurement::getRecorddate));
        XYChart.Series<String, Number> weightSeries = new XYChart.Series<>();
        weightSeries.setName("Weight (kg)");

        for (Measurement m : measurements) {
            String date = m.getRecorddate().format(DateTimeFormatter.ofPattern("MMM dd"));
            weightSeries.getData().add(new XYChart.Data<>(date, m.getWeight()));
        }

        if (!weightSeries.getData().isEmpty()) {
            weightBmiChart.getData().add(weightSeries);
        }

        // Populate calorie chart
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

        // Populate tables
        ObservableList<Measurement> measurementData = FXCollections.observableArrayList(measurements);
        measurementTable.setItems(measurementData);

        ObservableList<Workout> workoutData = FXCollections.observableArrayList(workouts);
        workoutTable.setItems(workoutData);

        System.out.println("DEBUG: Tables populated - Measurements: " + measurements.size() + ", Workouts: " + workouts.size());
    }

    /**
     * Get BMI category
     */
    private String getBMICategory(double bmi) {
        if (bmi < 18.5) return "Underweight";
        if (bmi < 25) return "Normal";
        if (bmi < 30) return "Overweight";
        return "Obese";
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
            System.err.println("Failed to load login_register FXML on logout.");
            e.printStackTrace();
        }
    }
}
