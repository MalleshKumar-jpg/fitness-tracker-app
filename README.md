
# Fitness Tracking System

* FitnessTrackerApp is a desktop fitness management application built using **Java**, **JavaFX**, and **Hibernate**. 
* It allows users to track workouts, monitor body measurements, and view progress reports — all within a clean and intuitive graphical interface.

---

## Overview

* This application is designed for users who want a structured way to manage and analyze their fitness progress.
* Users can create accounts, log workouts, record measurements, and view summaries of their activity history.
* All data is stored locally using Hibernate ORM, making the app fully standalone.

---

## Features

### User Management

* Register and log in securely
* Personalized fitness data for each user
* Hibernate-based persistent user storage

### Workout Logging

* Add workout entries with activity type, duration, intensity, date, and notes
* View, edit, and delete logged workouts
* Keep long-term exercise records

### Measurement Tracking

* Record metrics such as weight, height, and other optional stats
* Track progress over time
* Useful for monitoring changes in body composition

### Reports & Dashboard

* Summary of recent workouts and measurement trends
* Quick understanding of user activity
* A simple dashboard-like overview

### Modern JavaFX UI

* Screens built with FXML
* Includes login, registration, dashboard, workout log, measurement log, and report pages
* Lightweight, responsive navigation

---

## Architecture

The application follows a clean, maintainable structure:

**Model**
Contains entity classes (User, Workout, Measurement) mapped via Hibernate.

**DAO**
Handles database operations (save, update, query, delete).

**Service Layer**
Contains business logic and communicates with DAOs.

**Controller**
Manages screen behavior and user interactions.

**UI (FXML)**
Defines layout and visual components for each page.

---
## Project Structure

```
FitnessTrackerApp
├── src
│   └── main
│       ├── java
│       │   └── com
│       │       └── fitnesstracker
│       │           ├── controller            (JavaFX controllers)
│       │           ├── dao                   (Hibernate data access)
│       │           ├── model                 (Entity classes)
│       │           ├── service               (Business logic)
│       │           ├── App.java              (Main application entry)
│       │           └── HibernateUtil.java
│       └── resources
│           ├── *.fxml                       (FXML UI screens)
├── pom.xml                                   (Maven dependencies)
```

---

## How to Run

### 1. Install Requirements

* Java JDK (17 or your project's version)
* Maven
* JavaFX SDK (if your IDE requires manual setup)

### 2. Build the Project

Open a terminal in the project directory and run:

```
mvn clean install
```

### 3. Run the Application

You can run it with:

```
mvn javafx:run
```

Or run **App.java** directly from your IDE.
