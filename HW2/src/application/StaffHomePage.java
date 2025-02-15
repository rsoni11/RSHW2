
package application;

import databasePart1.DatabaseHelper;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Button; //***RS



//New file - added by Janelle

/**
 * This page displays a simple welcome message for users accessing the Staff role.
 */

public class StaffHomePage { // Make more classes for instructor, reviewer, staff

	private final DatabaseHelper databaseHelper;
	public StaffHomePage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }
	
	 public void show(Stage primaryStage, User user) {
    	VBox layout = new VBox();
	    layout.setStyle("-fx-alignment: center; -fx-padding: 20;");
	    
	    // Label to display Hello user
	    Label staffLabel = new Label("Hello, Staff!");
	    staffLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

	 // ***RS Return button to go back to RolePickMenuPage
	    Button returnButton = new Button("Return");
        returnButton.setOnAction(a -> {
            new RolePickMenuPage(databaseHelper).show(primaryStage, user); 
        });

        layout.getChildren().addAll(staffLabel, returnButton);
        Scene staffScene = new Scene(layout, 800, 400);

        // Set the scene to primary stage
        primaryStage.setScene(staffScene);
        primaryStage.setTitle("Staff Page");
    }
}