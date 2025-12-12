package com.fitnesstracker.service;

import com.fitnesstracker.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.jdbc.Work;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service for generating simplified fitness reports using stored procedures
 */
public class ReportService {

    private static class ReportData {
        int totalWorkouts;
        int totalCalories;
        int totalMeasurements;
        double latestWeight;
        double latestHeight;
        List<WorkoutData> workouts = new ArrayList<>();
        List<MeasurementData> measurements = new ArrayList<>();
    }

    private static class WorkoutData {
        int workoutId;
        LocalDate workoutDate;
        String workoutType;
        int durationMinutes;
        int caloriesBurned;
    }

    private static class MeasurementData {
        int measurementId;
        LocalDate recordDate;
        double weight;
        double height;
    }

    /**
     * Generate a simple text report for the user using stored procedure
     * @param userId The user's ID
     * @return Simple text report
     */
    public String generateReport(Long userId) {
        Session session = HibernateUtil.getSessionFactory().openSession();

        try {
            final ReportData reportData = new ReportData();

            session.doWork(new Work() {
                @Override
                public void execute(Connection connection) throws SQLException {
                    // Call the stored procedure
                    String sql = "{call GET_FITNESS_REPORT(?, ?, ?, ?, ?, ?, ?, ?)}";

                    try (CallableStatement stmt = connection.prepareCall(sql)) {
                        // Set input parameter
                        stmt.setLong(1, userId);

                        // Register output parameters
                        stmt.registerOutParameter(2, Types.NUMERIC); // total_workouts
                        stmt.registerOutParameter(3, Types.NUMERIC); // total_calories
                        stmt.registerOutParameter(4, Types.NUMERIC); // total_measurements
                        stmt.registerOutParameter(5, Types.NUMERIC); // latest_weight
                        stmt.registerOutParameter(6, Types.NUMERIC); // latest_height
                        stmt.registerOutParameter(7, Types.REF_CURSOR); // workouts cursor
                        stmt.registerOutParameter(8, Types.REF_CURSOR); // measurements cursor

                        // Execute procedure
                        stmt.execute();

                        // Get scalar outputs
                        reportData.totalWorkouts = stmt.getInt(2);
                        reportData.totalCalories = stmt.getInt(3);
                        reportData.totalMeasurements = stmt.getInt(4);
                        reportData.latestWeight = stmt.getDouble(5);
                        reportData.latestHeight = stmt.getDouble(6);

                        // Debug output
                        System.out.println("DEBUG: Total workouts = " + reportData.totalWorkouts);
                        System.out.println("DEBUG: Total measurements = " + reportData.totalMeasurements);

                        // Process workouts cursor
                        ResultSet workoutsRs = (ResultSet) stmt.getObject(7);
                        if (workoutsRs != null) {
                            try {
                                int workoutCount = 0;
                                while (workoutsRs.next()) {
                                    WorkoutData workout = new WorkoutData();
                                    workout.workoutId = workoutsRs.getInt("WORKOUTID");
                                    workout.workoutDate = workoutsRs.getDate("WORKOUTDATE").toLocalDate();
                                    workout.workoutType = workoutsRs.getString("WORKOUTTYPE");
                                    workout.durationMinutes = workoutsRs.getInt("DURATIONMINUTES");
                                    workout.caloriesBurned = workoutsRs.getInt("CALORIESBURNED");
                                    reportData.workouts.add(workout);
                                    workoutCount++;
                                }
                                System.out.println("DEBUG: Retrieved " + workoutCount + " workouts from cursor");
                            } finally {
                                workoutsRs.close();
                            }
                        } else {
                            System.out.println("DEBUG: Workouts cursor is null");
                        }

                        // Process measurements cursor
                        ResultSet measurementsRs = (ResultSet) stmt.getObject(8);
                        if (measurementsRs != null) {
                            try {
                                int measurementCount = 0;
                                while (measurementsRs.next()) {
                                    MeasurementData measurement = new MeasurementData();
                                    measurement.measurementId = measurementsRs.getInt("MEASUREMENTID");
                                    measurement.recordDate = measurementsRs.getDate("RECORDDATE").toLocalDate();
                                    measurement.weight = measurementsRs.getDouble("WEIGHT");
                                    measurement.height = measurementsRs.getDouble("HEIGHT");
                                    reportData.measurements.add(measurement);
                                    measurementCount++;
                                }
                                System.out.println("DEBUG: Retrieved " + measurementCount + " measurements from cursor");
                            } finally {
                                measurementsRs.close();
                            }
                        } else {
                            System.out.println("DEBUG: Measurements cursor is null");
                        }
                    }
                }
            });

            return buildReportString(reportData);

        } catch (Exception e) {
            System.err.println("ERROR: Failed to generate simple report: " + e.getMessage());
            e.printStackTrace();
            return "Error generating report: " + e.getMessage();
        } finally {
            session.close();
        }
    }

    private String buildReportString(ReportData data) {
        StringBuilder report = new StringBuilder();
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Header
        report.append("--- COMPREHENSIVE FITNESS SUMMARY ---\n");
        report.append("Metric        Value\n");
        report.append("-------------------------------------\n");

        // Summary metrics
        report.append(String.format("Total Workouts      %d\n", data.totalWorkouts));
        report.append(String.format("Total Calories      %d\n", data.totalCalories));
        report.append(String.format("Total Measurements  %d\n", data.totalMeasurements));

        // Latest measurements
        if (data.latestWeight > 0) {
            report.append(String.format("Latest Weight       %.0f\n", data.latestWeight));

            // Calculate BMI
            double heightInMeters = data.latestHeight / 100.0;
            double bmi = data.latestWeight / (heightInMeters * heightInMeters);
            report.append(String.format("Latest BMI          %.1f\n", bmi));

            // BMI Category
            String bmiCategory;
            if (bmi < 18.5) {
                bmiCategory = "Underweight";
            } else if (bmi < 25) {
                bmiCategory = "Normal";
            } else if (bmi < 30) {
                bmiCategory = "Overweight";
            } else {
                bmiCategory = "Obese";
            }
            report.append(String.format("Health Status       %s\n", bmiCategory));
        } else {
            report.append("Latest Weight       N/A\n");
            report.append("Latest BMI          N/A\n");
            report.append("Health Status       N/A\n");
        }

        report.append("\n");

        // Workouts section
        report.append("--- DETAILED WORKOUTS ---\n");
        report.append("Workout ID  Date        Type        Duration (min)  Calories Burned\n");
        report.append("-------------------------------------------------------------------\n");

        if (data.workouts.isEmpty()) {
            report.append("No workouts recorded yet.\n");
        } else {
            for (WorkoutData workout : data.workouts) {
                report.append(String.format("    %3d     %s  %-12s    %3d            %d\n",
                        workout.workoutId,
                        workout.workoutDate.format(dateFormat),
                        workout.workoutType,
                        workout.durationMinutes,
                        workout.caloriesBurned
                ));
            }
        }

        report.append("\n");

        // Measurements section
        report.append("--- DETAILED MEASUREMENTS ---\n");
        report.append("Measurement ID  Date        Weight (kg)  Height (cm)  BMI\n");
        report.append("----------------------------------------------------------\n");

        if (data.measurements.isEmpty()) {
            report.append("No measurements recorded yet.\n");
        } else {
            for (MeasurementData measurement : data.measurements) {
                double heightInMeters = measurement.height / 100.0;
                double bmi = measurement.weight / (heightInMeters * heightInMeters);

                report.append(String.format("     %3d       %s      %.0f           %.0f       %.1f\n",
                        measurement.measurementId,
                        measurement.recordDate.format(dateFormat),
                        measurement.weight,
                        measurement.height,
                        bmi
                ));
            }
        }

        return report.toString();
    }

    /**
     * Export simple report to a text file
     * @param userId The user's ID
     * @param filename The filename to save the report
     * @return true if successful, false otherwise
     */
    public boolean exportSimpleReport(Long userId, String filename) {
        try {
            String report = generateReport(userId);
            java.io.FileWriter writer = new java.io.FileWriter(filename);
            writer.write(report);
            writer.close();
            return true;
        } catch (Exception e) {
            System.err.println("ERROR: Failed to export report: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}