package com.fitnesstracker;

import com.fitnesstracker.model.User;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.hibernate.SessionFactory;

import java.io.IOException;

/**
 * Main application entry point for the Fitness Tracker application (JavaFX).
 * Manages the primary stage, scene switching, and the application's current user session.
 */
public class App extends Application {

    // Static reference to the primary stage for scene switching
    private static Stage primaryStage;

    private static User currentUser;

    /**
     * The primary method for all JavaFX applications. Stores the stage reference.
     */
    @Override
    public void start(Stage stage) throws IOException {
        App.primaryStage = stage;

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
        Parent root = loader.load();

        // Set up the primary stage (window)
        primaryStage.setTitle("Fitness Tracker - Login/Register");
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    /**
     * Static method to switch the content (root) of the current scene to a new FXML view.
     * @param fxml The name of the FXML file (e.g., "Dashboard" loads Dashboard.fxml)
     */
    public static void setRoot(String fxml) throws IOException {
        FXMLLoader loader = new FXMLLoader(App.class.getResource("/" + fxml + ".fxml"));
        Parent root = loader.load();
        primaryStage.getScene().setRoot(root);

        primaryStage.setTitle("Fitness Tracker - " + fxml);
    }

    public static void showDashboard(User user) throws Exception {
        // CRITICAL: Set current user BEFORE loading FXML
        // This ensures DashboardController.initialize() sees the correct user
        setCurrentUser(user);

        // Create a NEW FXMLLoader instance to ensure fresh controller
        FXMLLoader loader = new FXMLLoader(App.class.getResource("/Dashboard.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("Fitness Tracker - Dashboard");

        // ALWAYS create a new Scene to avoid stale controller references
        Scene scene = new Scene(root, 1600, 1000);
        primaryStage.setScene(scene);

        primaryStage.setResizable(true);
        primaryStage.setMinWidth(1200);
        primaryStage.setMinHeight(800);

        // SET PREFERRED SIZE
        primaryStage.setWidth(1600);
        primaryStage.setHeight(1000);
        primaryStage.show();
    }
    /**
     * Retrieves the currently logged-in user.
     * @return The User object or null if no user is logged in.
     */
    public static User getCurrentUser() {
        return currentUser;
    }

    /**
     * Sets the user who just logged in.
     * @param user The User object to set as the current session user.
     */
    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    /**
     * Clears the current user session and returns to the login screen.
     */
    public static void logout() throws IOException {
        currentUser = null; // Clear the user session
        // FIX: Use the corrected FXML name: 'login_register'
        setRoot("login"); // Go back to login screen
        primaryStage.setTitle("Fitness Tracker - Login/Register");
    }

    // This is the main method that starts the Java application
    // This is the main method that starts the Java application
    public static void main(String[] args) {
        // Initialize the Hibernate Session Factory (Database connection)
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory(); // CHANGED: Directly get the session factory

        if (sessionFactory == null) {
            System.err.println("FATAL: Could not initialize Hibernate Session Factory. Database setup failed.");
            return;
        }

        System.out.println("INFO: Hibernate Session Factory initialized successfully.");

        // Launch the JavaFX application
        Application.launch(App.class, args);

        // Shutdown when the application closes
        sessionFactory.close();
        System.out.println("INFO: Hibernate Session Factory closed.");
    }
}