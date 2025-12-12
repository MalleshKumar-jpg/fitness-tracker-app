package com.fitnesstracker.model;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Measurement entity - Simplified version without body fat percentage
 */
@Entity
@Table(name = "MEASUREMENT")
public class Measurement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MEASUREMENTID")
    private Long measurementId;

    @ManyToOne
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @Column(name = "RECORDDATE", nullable = false)
    private LocalDate recorddate;  // ✅ Remove @Temporal - LocalDate works directly

    @Column(name = "WEIGHT")
    private Double weight;

    @Column(name = "HEIGHT")
    private Double height;

    // Constructors
    public Measurement() {}

    public Measurement(User user, LocalDate recorddate, Double weight, Double height) {
        this.user = user;
        this.recorddate = recorddate;
        this.weight = weight;
        this.height = height;
    }

    // Getters and Setters
    public Long getMeasurementId() {
        return measurementId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDate getRecorddate() {
        return recorddate;
    }

    public void setRecorddate(LocalDate recorddate) {
        this.recorddate = recorddate;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }
}