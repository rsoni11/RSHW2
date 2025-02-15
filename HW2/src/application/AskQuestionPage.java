package application;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import databasePart1.DatabaseHelper;
import application.Question;
import application.QuestionsDO;
import java.sql.SQLException; 
import java.util.List;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class AskQuestionPage {
    private final DatabaseHelper databaseHelper;
    private final User user;

    public AskQuestionPage(DatabaseHelper databaseHelper, User user) {
        this.databaseHelper = databaseHelper;
        this.user = user;
    }

    public void show(Stage primaryStage) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        Label titleLabel = new Label("Ask a New Question");
        TextField titleField = new TextField();
        titleField.setPromptText("Enter question title");

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Enter question description");

        Button submitButton = new Button("Submit");
        submitButton.setOnAction(e -> {
            String title = titleField.getText();
            String description = descriptionArea.getText();
            if (!title.isEmpty() && !description.isEmpty()) {
                try {
                    Question question = new Question(0, title, description, user.getUserName());
                    new QuestionsDO(databaseHelper).createQuestion(question);
                    primaryStage.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            } else {
                // Display error message if question is empty
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Error message: question cannot be empty.");
                alert.showAndWait();
            }
        });

        layout.getChildren().addAll(titleLabel, titleField, descriptionArea, submitButton);

        Scene scene = new Scene(layout, 800, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Ask a Question");
    }
}