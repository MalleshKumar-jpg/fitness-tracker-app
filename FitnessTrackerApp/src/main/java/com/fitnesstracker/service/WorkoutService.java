package com.fitnesstracker.service;

import com.fitnesstracker.dao.ActivityDAO;
import com.fitnesstracker.model.Workout;
import java.util.List;

/**
 * Service layer for handling business logic and persistence operations
 * related to workout tracking.
 */
public class WorkoutService {
    private final ActivityDAO activityDAO = new ActivityDAO();

    /**
     * Validates and logs a new workout.
     * @param workout The workout entity to save.
     */
    public void logWorkout(Workout workout) {
        if (workout.getCaloriesBurned() < 0 || workout.getDurationMinutes() <= 0) {
            throw new IllegalArgumentException("Invalid workout data: Duration must be positive and calories must be non-negative.");
        }
        activityDAO.save(workout);
    }

    /**
     * Retrieves all workouts for a specific user.
     * @param userId The ID of the user.
     * @return A list of Workout objects.
     */
    public List<Workout> getWorkoutsByUser(Long userId) {
        return activityDAO.findAllWorkoutsByUserId(userId);
    }
}
