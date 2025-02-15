package application;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import databasePart1.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import javafx.scene.layout.HBox;

public class StudentHomePage {
    private final DatabaseHelper databaseHelper;
    private final QuestionsDO questionsDO;
    private final AnswerDO answerDO;

    public StudentHomePage(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
        this.questionsDO = new QuestionsDO(databaseHelper);
        this.answerDO = new AnswerDO(databaseHelper);
    }

    public void show(Stage primaryStage, User user) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        // Label to display Hello user
        Label studentLabel = new Label("Hello, Student!");
        studentLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Buttons
        Button searchButton = new Button("Search");
        Button allQuestionsButton = new Button("All Questions"); // Renamed from unresolvedButton
        Button askButton = new Button("Ask");
        Button myQuestionsButton = new Button("My Questions");
        Button returnButton = new Button("Return");

        // Event Handlers for Buttons
        searchButton.setOnAction(e -> handleSearch(primaryStage, user));
        allQuestionsButton.setOnAction(e -> handleAllQuestions(primaryStage, user)); // Updated handler
        askButton.setOnAction(e -> handleAskQuestion(primaryStage, user));
        myQuestionsButton.setOnAction(e -> handleMyQuestions(primaryStage, user));
        returnButton.setOnAction(e -> new RolePickMenuPage(databaseHelper).show(primaryStage, user));

        // Add components to layout
        layout.getChildren().addAll(studentLabel, searchButton, allQuestionsButton, askButton, myQuestionsButton, returnButton);

        // Set scene and stage
        Scene studentScene = new Scene(layout, 800, 400);
        primaryStage.setScene(studentScene);
        primaryStage.setTitle("Student Page");
    }

    // Event Handlers
    private void handleSearch(Stage primaryStage, User user) {
        // Open a new window for search
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        Label searchLabel = new Label("Enter a keyword to search:");
        TextField searchField = new TextField();
        searchField.setPromptText("Search...");

        Button searchButton = new Button("Search");
        searchButton.setOnAction(e -> {
            String keyword = searchField.getText().trim();
            if (!keyword.isEmpty()) {
                try {
                    List<Question> matchingQuestions = questionsDO.searchQuestions(keyword);
                    if (matchingQuestions.isEmpty()) {
                        Alert alert = new Alert(AlertType.INFORMATION);
                        alert.setTitle("Search Results");
                        alert.setHeaderText(null);
                        alert.setContentText("No questions found containing: " + keyword);
                        alert.showAndWait();
                    } else {
                        displayQuestions(primaryStage, matchingQuestions, "Search Results");
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> show(primaryStage, user));

        layout.getChildren().addAll(searchLabel, searchField, searchButton, backButton);

        Scene searchScene = new Scene(layout, 800, 400);
        primaryStage.setScene(searchScene);
        primaryStage.setTitle("Search Questions");
    }

    private void handleAllQuestions(Stage primaryStage, User user) {
        // Display all questions (both resolved and unresolved)
        try {
            List<Question> allQuestions = questionsDO.readQuestions();
            if (allQuestions.isEmpty()) {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("All Questions");
                alert.setHeaderText(null);
                alert.setContentText("No questions found.");
                alert.showAndWait();
            } else {
                displayQuestions(primaryStage, allQuestions, "All Questions");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleAskQuestion(Stage primaryStage, User user) {
        // Open a new window to ask a question
        AskQuestionPage askQuestionPage = new AskQuestionPage(databaseHelper, user);
        askQuestionPage.show(primaryStage);
    }

    private void handleMyQuestions(Stage primaryStage, User user) {
        // Display questions asked by the current user
        try {
            List<Question> myQuestions = questionsDO.readQuestions().stream()
                    .filter(q -> q.getAuthor().equals(user.getUserName()))
                    .collect(Collectors.toList());

            if (myQuestions.isEmpty()) {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("My Questions");
                alert.setHeaderText(null);
                alert.setContentText("You have not asked any questions yet.");
                alert.showAndWait();
            } else {
                displayMyQuestions(primaryStage, myQuestions, "My Questions", user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void displayQuestions(Stage primaryStage, List<Question> questions, String title) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        for (Question question : questions) {
            Button questionButton = new Button(question.getTitle());
            questionButton.setOnAction(e -> displayQuestionWithReplies(primaryStage, question, new User("", "", question.getAuthor(), "", new ArrayList<>())));
            layout.getChildren().add(questionButton);
        }

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> show(primaryStage, new User("", "", "", "", new ArrayList<>())));
        layout.getChildren().add(backButton);

        Scene scene = new Scene(layout, 800, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle(title);
    }
    
    private void deleteReply(Stage primaryStage, Question question, Answer answer, User user) {
        try {
            answerDO.deleteAnswer(answer.getId()); // Use AnswerDO to delete the answer
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Your reply has been deleted.");
            alert.showAndWait();
            displayQuestionWithReplies(primaryStage, question, user); // Refresh the page
        } catch (SQLException ex) {
            ex.printStackTrace();
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("An error occurred while deleting the reply.");
            alert.showAndWait();
        }
    }

    private void displayQuestionWithReplies(Stage primaryStage, Question question, User user) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        Label titleLabel = new Label("Question: " + question.getTitle());
        Label descriptionLabel = new Label("Description: " + question.getDescription());

        // Display all replies
        try {
            List<Answer> answers = answerDO.readAnswers(question.getId());
            if (answers.isEmpty()) {
                Label noAnswersLabel = new Label("No replies yet.");
                layout.getChildren().add(noAnswersLabel);
            } else {
                Label repliesLabel = new Label("Replies:");
                layout.getChildren().add(repliesLabel);
                for (Answer answer : answers) {
                    VBox replyBox = new VBox(5);
                    replyBox.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-padding: 10;");

                    // Display the reply author and content
                    Label answerLabel = new Label("Reply by " + answer.getAuthor() + ": " + answer.getContent());
                    replyBox.getChildren().add(answerLabel);

                    // Add Edit and Delete buttons only for the author of the reply
                    if (answer.getAuthor().equals(user.getUserName())) {
                        HBox buttonBox = new HBox(5);
                        Button editButton = new Button("Edit");
                        editButton.setOnAction(e -> editReply(primaryStage, question, answer, user));

                        Button deleteButton = new Button("Delete");
                        deleteButton.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white;");
                        deleteButton.setOnAction(e -> deleteReply(primaryStage, question, answer, user));

                        buttonBox.getChildren().addAll(editButton, deleteButton);
                        replyBox.getChildren().add(buttonBox);
                    }

                    layout.getChildren().add(replyBox);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Add a text area for replying
        TextArea replyArea = new TextArea();
        replyArea.setPromptText("Enter your reply");

        Button submitButton = new Button("Submit Reply");
        submitButton.setOnAction(e -> {
            String replyText = replyArea.getText();
            if (!replyText.isEmpty()) {
                try {
                    // Ensure the author is set to the current user's username
                    Answer answer = new Answer(0, question.getId(), replyText, user.getUserName(), false);
                    answerDO.createAnswer(answer);
                    Alert alert = new Alert(AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText(null);
                    alert.setContentText("Your reply has been submitted.");
                    alert.showAndWait();
                    displayQuestionWithReplies(primaryStage, question, user); // Refresh the page
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            } else {
                // Display error message if reply is empty
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Error message: reply cannot be empty.");
                alert.showAndWait();
            }
        });

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> handleAllQuestions(primaryStage, user));

        layout.getChildren().addAll(titleLabel, descriptionLabel, replyArea, submitButton, backButton);

        Scene scene = new Scene(layout, 800, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Question Details");
    }

    private void editReply(Stage primaryStage, Question question, Answer answer, User user) {
        if (!answer.getAuthor().equals(user.getUserName())) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("You do not have permission to edit this reply.");
            alert.showAndWait();
            return;
        }

        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        Label titleLabel = new Label("Edit Reply");
        TextArea replyArea = new TextArea(answer.getContent());
        replyArea.setPromptText("Edit your reply");

        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> {
            String updatedReply = replyArea.getText();
            if (!updatedReply.isEmpty()) {
                try {
                    answer.setContent(updatedReply);
                    answerDO.updateAnswer(answer);
                    Alert alert = new Alert(AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText(null);
                    alert.setContentText("Your reply has been updated.");
                    alert.showAndWait();
                    displayQuestionWithReplies(primaryStage, question, user); // Refresh the page
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> displayQuestionWithReplies(primaryStage, question, user));

        layout.getChildren().addAll(titleLabel, replyArea, saveButton, backButton);

        Scene scene = new Scene(layout, 800, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Edit Reply");
    }

    private void displayMyQuestions(Stage primaryStage, List<Question> questions, String title, User user) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        for (Question question : questions) {
            HBox questionBox = new HBox(10);
            questionBox.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-padding: 10;");

            Button questionButton = new Button(question.getTitle());
            questionButton.setOnAction(e -> manageMyQuestion(primaryStage, question, user));

            // Add Delete button if the question belongs to the current user
            if (question.getAuthor().equals(user.getUserName())) {
                Button deleteButton = new Button("Delete");
                deleteButton.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white;");
                deleteButton.setOnAction(e -> deleteQuestion(primaryStage, question, user));
                questionBox.getChildren().addAll(questionButton, deleteButton);
            } else {
                questionBox.getChildren().add(questionButton);
            }

            layout.getChildren().add(questionBox);
        }

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> show(primaryStage, user));
        layout.getChildren().add(backButton);

        Scene scene = new Scene(layout, 800, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle(title);
    }

    private void manageMyQuestion(Stage primaryStage, Question question, User user) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        Label titleLabel = new Label("Title: " + question.getTitle());
        Label descriptionLabel = new Label("Description: " + question.getDescription());
        

        Button editButton = new Button("Edit");
        editButton.setOnAction(e -> editQuestion(primaryStage, question, user));

        // Add Delete button
        Button deleteButton = new Button("Delete");
        deleteButton.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white;");
        deleteButton.setOnAction(e -> deleteQuestion(primaryStage, question, user));

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> handleMyQuestions(primaryStage, user));

        layout.getChildren().addAll(titleLabel, descriptionLabel, editButton, deleteButton, backButton);

        Scene scene = new Scene(layout, 800, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Manage Question");
    }

    private void editQuestion(Stage primaryStage, Question question, User user) {
        VBox layout = new VBox(10);
        layout.setStyle("-fx-alignment: center; -fx-padding: 20;");

        Label titleLabel = new Label("Edit Question");
        TextField titleField = new TextField(question.getTitle());
        TextArea descriptionArea = new TextArea(question.getDescription());

        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> {
            String newTitle = titleField.getText();
            String newDescription = descriptionArea.getText();
            if (!newTitle.isEmpty() && !newDescription.isEmpty()) {
                try {
                    question.setTitle(newTitle);
                    question.setDescription(newDescription);
                    questionsDO.updateQuestion(question);
                    Alert alert = new Alert(AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText(null);
                    alert.setContentText("Your question has been updated.");
                    alert.showAndWait();
                    handleMyQuestions(primaryStage, user);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        Button backButton = new Button("Back");
        backButton.setOnAction(e -> manageMyQuestion(primaryStage, question, user));

        layout.getChildren().addAll(titleLabel, titleField, descriptionArea, saveButton, backButton);

        Scene scene = new Scene(layout, 800, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Edit Question");
    }
    
    private void deleteQuestion(Stage primaryStage, Question question, User user) {
        try {
            // Delete the question from the database
            questionsDO.deleteQuestion(question.getId());

            // Show success message
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("The question has been deleted.");
            alert.showAndWait();

            // Refresh the "My Questions" page
            handleMyQuestions(primaryStage, user);
        } catch (SQLException ex) {
            ex.printStackTrace();

            // Show error message
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("An error occurred while deleting the question.");
            alert.showAndWait();
        }
    }
}