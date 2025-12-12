package com.fitnesstracker.controller;

import com.fitnesstracker.model.User;
import com.fitnesstracker.service.ReportService;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Controller for displaying fitness report
 */
public class ReportController {

    @FXML private TextArea reportTextArea;
    @FXML private Label statusLabel;
    @FXML private Button exportButton;
    @FXML private Button refreshButton;

    private User currentUser;
    private final ReportService reportService = new ReportService();
    private String currentReport;

    /**
     * Initialize the controller
     */
    @FXML
    public void initialize() {
        // Set monospace font for the report text area
        reportTextArea.setFont(Font.font("Courier New", 12));
        reportTextArea.setEditable(false);
        reportTextArea.setWrapText(false);
    }

    /**
     * Set the user and load the report
     */
    public void setUser(User user) {
        this.currentUser = user;
        loadReport();
    }

    /**
     * Load and display the report
     */
    @FXML
    private void loadReport() {
        try {
            if (currentUser == null) {
                statusLabel.setText("No user selected");
                statusLabel.setStyle("-fx-text-fill: red;");
                return;
            }

            statusLabel.setText("Loading report...");
            statusLabel.setStyle("-fx-text-fill: black;");

            // Generate the report
            currentReport = reportService.generateReport(currentUser.getUserId());

            // Display in text area
            reportTextArea.setText(currentReport);

            statusLabel.setText("Report loaded successfully");
            statusLabel.setStyle("-fx-text-fill: green;");

        } catch (Exception e) {
            statusLabel.setText("Error loading report: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        }
    }

    /**
     * Export report to a text file
     */
    @FXML
    private void handleExport() {
        if (currentReport == null || currentReport.isEmpty()) {
            statusLabel.setText("No report to export");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Report as Text File");
        fileChooser.setInitialFileName("fitness_report_" + currentUser.getUsername() + ".txt");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        Stage stage = (Stage) exportButton.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(currentReport);
                statusLabel.setText("Report exported to: " + file.getName());
                statusLabel.setStyle("-fx-text-fill: green;");
            } catch (IOException e) {
                statusLabel.setText("Error exporting report: " + e.getMessage());
                statusLabel.setStyle("-fx-text-fill: red;");
                e.printStackTrace();
            }
        }
    }

    /**
     * Close the report window
     */
    @FXML
    private void handleClose() {
        Stage stage = (Stage) exportButton.getScene().getWindow();
        stage.close();
    }
}