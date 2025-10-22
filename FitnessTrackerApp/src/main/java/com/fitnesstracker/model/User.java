package com.fitnesstracker.model;

import jakarta.persistence.*;
import java.util.Set;

@Entity
@Table(name = "APP_USER")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USERID")
    private Long userId;

    @Column(name = "NAME", nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "PASSWORD", nullable = false, length = 50)
    private String password;

    @Column(name = "AGE", nullable = false, precision = 3)  // NUMBER(3) - up to 999
    private Integer age;

    @Column(name = "GENDER", nullable = false, length = 6)  // VARCHAR2(6) - fits "Female"
    private String gender;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Workout> workouts;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Measurement> measurements;

    // Constructors
    public User() {}

    public User(String name, String password, Integer age, String gender) {
        this.name = name;
        this.password = password;
        this.age = age;
        this.gender = gender;
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

}