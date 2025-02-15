package application;
import databasePart1.DatabaseHelper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class QuestionsDO {
    private final DatabaseHelper databaseHelper;

    // Constructor
    public QuestionsDO(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    // Create a new question
    public void createQuestion(Question question) throws SQLException {
        if (databaseHelper.getConnection() == null) {
            throw new SQLException("Database connection is not established.");
        }

        String query = "INSERT INTO Questions (title, description, author, isResolved) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(query)) {
            pstmt.setString(1, question.getTitle());
            pstmt.setString(2, question.getDescription());
            pstmt.setString(3, question.getAuthor());
            pstmt.setBoolean(4, false); // Default value for isResolved is FALSE
            pstmt.executeUpdate();
        }
    }

    // Read all questions
    public List<Question> readQuestions() throws SQLException {
        if (databaseHelper.getConnection() == null) {
            throw new SQLException("Database connection is not established.");
        }

        List<Question> questions = new ArrayList<>();
        String query = "SELECT * FROM Questions";
        try (Statement stmt = databaseHelper.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                questions.add(new Question(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("author")
                ));
            }
        }
        return questions;
    }

    // Update a question
    public void updateQuestion(Question question) throws SQLException {
        if (databaseHelper.getConnection() == null) {
            throw new SQLException("Database connection is not established.");
        }

        String query = "UPDATE Questions SET title = ?, description = ? WHERE id = ? AND isResolved = FALSE";
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(query)) {
            pstmt.setString(1, question.getTitle());
            pstmt.setString(2, question.getDescription());
            pstmt.setInt(3, question.getId());
            pstmt.executeUpdate();
        }
    }

    // Delete a question
    public void deleteQuestion(int questionId) throws SQLException {
        if (databaseHelper.getConnection() == null) {
            throw new SQLException("Database connection is not established.");
        }

        String query = "DELETE FROM Questions WHERE id = ? AND isResolved = FALSE";
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(query)) {
            pstmt.setInt(1, questionId);
            pstmt.executeUpdate();
        }
    }

    // Search questions by keyword
    public List<Question> searchQuestions(String keyword) throws SQLException {
        if (databaseHelper.getConnection() == null) {
            throw new SQLException("Database connection is not established.");
        }

        List<Question> questions = new ArrayList<>();
        String query = "SELECT * FROM Questions WHERE title LIKE ? OR description LIKE ?";
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(query)) {
            pstmt.setString(1, "%" + keyword + "%");
            pstmt.setString(2, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                questions.add(new Question(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("author")
                ));
            }
        }
        return questions;
    }
}