package com.fitnesstracker.model;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Maps to the Workout table. Linked to User via user_id.
 */
@Entity
public class Workout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long workoutId;

    // Foreign Key: Links this workout back to one User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // The actual User object linked by user_id FK

    @Column(nullable = false)
    private LocalDate workoutdate;

    @Column(name = "WORKOUTTYPE", length = 20, nullable = false)
    private String workoutType;

    private Long durationMinutes; // Using "Minutes" to be explicit
    private Integer caloriesBurned;

    public Workout() {}

    // Constructor for easy creation
    public Workout(User user, LocalDate workoutdate, String workoutType, Long durationMinutes, Integer caloriesBurned) {
        this.user = user;
        this.workoutdate = workoutdate;
        this.workoutType = workoutType;
        this.durationMinutes = durationMinutes;
        this.caloriesBurned = caloriesBurned;
    }

    // --- Getters and Setters ---
    public Long getWorkoutId() { return workoutId; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public LocalDate getWorkoutDate() { return workoutdate; }
    public void setWorkoutDate(LocalDate date) { this.workoutdate = date; }
    public String getWorkoutType() { return workoutType; }
    public void setWorkoutType(String workoutType) { this.workoutType = workoutType; }
    public Long getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Long durationMinutes) { this.durationMinutes = durationMinutes; }
    public Integer getCaloriesBurned() { return caloriesBurned; }
    public void setCaloriesBurned(Integer caloriesBurned) { this.caloriesBurned = caloriesBurned; }
}