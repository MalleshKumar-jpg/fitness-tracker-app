package com.fitnesstracker;

import com.fitnesstracker.model.User;
import com.fitnesstracker.HibernateUtil;
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
        setCurrentUser(user);

        FXMLLoader loader = new FXMLLoader(App.class.getResource("/Dashboard.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("Fitness Tracker - Dashboard");

        // Create a new Scene if one doesn't exist, or replace the root of the existing one
        Scene scene = primaryStage.getScene();
        if (scene == null) {
            scene = new Scene(root, 1000, 700);
            primaryStage.setScene(scene);
        } else {
            scene.setRoot(root);
        }

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
    public static void main(String[] args) {
        //to initialize the Hibernate Session Factory (Database connection)
        SessionFactory sessionFactory = null;
        try {
            sessionFactory = HibernateUtil.getSessionFactory();
            System.out.println("INFO: Hibernate Session Factory initialized successfully.");
        } catch (Exception e) {
            System.err.println("FATAL: Could not initialize Hibernate Session Factory. Database setup failed.");
            e.printStackTrace();
            //exits the application if the database connection fails
            return;
        }

        // If the database initialization is successful
        Application.launch(App.class, args);

        // 3. Shutdown when the application closes
        if (sessionFactory != null) {
            sessionFactory.close();
            System.out.println("INFO: Hibernate Session Factory closed.");
        }
    }
}
