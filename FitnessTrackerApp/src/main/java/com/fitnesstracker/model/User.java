package com.fitnesstracker.model;

import jakarta.persistence.*;
import java.util.Set;

/**
 * Maps to the USER table (renamed to APP_USER to avoid Oracle keyword conflicts).
 * This is the parent entity for workouts and measurements.
 */
@Entity
@Table(name = "APP_USER")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USERID")
    private Long userId;

    @Column(name = "USERNAME", length = 50, nullable = false, unique = true)
    private String username;

    @Column(name = "NAME", length = 100)
    private String name;

    @Column(name = "AGE")
    private Integer age;

    @Column(name = "PASSWORD", length = 50, nullable = false)
    private String password;

    @Column(name = "GENDER", length = 6, nullable = false)
    private String gender;

    // One-to-Many relationships defined for navigation
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Workout> workouts;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Measurement> measurements;

    public User() {}

    // --- Getters and Setters ---
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    // Getters for workouts and measurements
    public Set<Workout> getWorkouts() { return workouts; }
    public Set<Measurement> getMeasurements() { return measurements; }
}