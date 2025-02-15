//Created by Rhea Soni
package application;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper;
import java.sql.SQLException;

public class RoleSelectionPage {
    private final DatabaseHelper databaseHelper;  //Database operations
    private final User user;					  //Stores user information
    
    
 // Constructor initializing database helper and user
    public RoleSelectionPage(DatabaseHelper databaseHelper, User user) { 		
        this.databaseHelper = databaseHelper;									
        this.user = user;
    }

    public void show(Stage primaryStage) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20; -fx-alignment: center;");

        Label titleLabel = new Label("Select Your Roles");  //Title
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        
        //Checkboxes for the roles
        CheckBox studentCheckBox = new CheckBox("Student");
        CheckBox instructorCheckBox = new CheckBox("Instructor");
        CheckBox staffCheckBox = new CheckBox("Staff");
        CheckBox reviewerCheckBox = new CheckBox("Reviewer");

        Button submitButton = new Button("Submit");
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        
        //submitButton
        submitButton.setOnAction(a -> {
            boolean atLeastOneSelected = studentCheckBox.isSelected() ||  // Check if at least one role is selected
                                         instructorCheckBox.isSelected() || 
                                         staffCheckBox.isSelected() || 
                                         reviewerCheckBox.isSelected();

            if (!atLeastOneSelected) {
                errorLabel.setText("You must select at least one role.");  // Show error if no roles are selected
            } else {
                try {
                    // Save roles to the database
                    if (studentCheckBox.isSelected()) {
                        databaseHelper.addUserRole(user, "student");
                    }
                    if (instructorCheckBox.isSelected()) {
                        databaseHelper.addUserRole(user, "instructor");
                    }
                    if (staffCheckBox.isSelected()) {
                        databaseHelper.addUserRole(user, "staff");
                    }
                    if (reviewerCheckBox.isSelected()) {
                        databaseHelper.addUserRole(user, "reviewer");
                    }

                    // Show success message and navigate to the home page
                    errorLabel.setText("Roles added successfully!");
                    new WelcomeLoginPage(databaseHelper).show(primaryStage, user);
                } catch (SQLException e) {
                    errorLabel.setText("Database error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        layout.getChildren().addAll(titleLabel, studentCheckBox, instructorCheckBox, staffCheckBox, reviewerCheckBox, submitButton, errorLabel);
        Scene roleSelectionScene = new Scene(layout, 800, 400);

        primaryStage.setScene(roleSelectionScene);
        primaryStage.setTitle("Role Selection");
        primaryStage.show();
    }
}