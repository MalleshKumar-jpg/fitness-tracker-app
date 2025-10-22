package com.fitnesstracker.service;

import com.fitnesstracker.dao.ActivityDAO;
import com.fitnesstracker.model.Measurement;
import java.util.List;

/**
 * Service layer for handling business logic and persistence operations
 * related to body measurements (e.g., validation, BMI calculation).
 */
public class MeasurementService {
    private final ActivityDAO activityDAO = new ActivityDAO();

    /**
     * Validates and logs a new body measurement.
     * @param measurement The measurement entity to save.
     */
    public void logMeasurement(Measurement measurement) {
        if (measurement.getWeight() <= 0 || measurement.getHeight() <= 0) {
            throw new IllegalArgumentException("Invalid measurement data: Weight and height must be positive values.");
        }
        activityDAO.save(measurement);
    }

    /**
     * Retrieves all measurements for a specific user.
     * @param userId The ID of the user.
     * @return A list of Measurement objects.
     */
    public List<Measurement> getMeasurementsByUser(Long userId) {
        return activityDAO.findAllMeasurementsByUserId(userId);
    }

    /**
     * Calculates the Body Mass Index (BMI) based on a measurement record.
     * @param measurement The measurement object containing weight (kg) and height (cm).
     * @return The calculated BMI value.
     */
    public double calculateBmi(Measurement measurement) {
        if (measurement.getHeight() == null || measurement.getHeight() <= 0 || measurement.getWeight() == null) {
            return 0.0;
        }
        // Height is in cm, convert to meters (m)
        double heightM = measurement.getHeight() / 100.0;
        // BMI Formula: weight (kg) / height^2 (m^2)
        double bmi = measurement.getWeight() / (heightM * heightM);

        // Round to one decimal place for presentation
        return Math.round(bmi * 10.0) / 10.0;
    }
}
