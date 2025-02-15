//Created by Rhea Soni
package application;
import databasePart1.DatabaseHelper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AnswerDO {
    private final DatabaseHelper databaseHelper;

    public AnswerDO(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    // Create a new answer
    public void createAnswer(Answer answer) throws SQLException {
        if (databaseHelper.getConnection() == null) {
            throw new SQLException("Database connection is not established.");
        }

        String query = "INSERT INTO Answers (questionId, content, author, isSolution) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(query)) {
            pstmt.setInt(1, answer.getQuestionId());
            pstmt.setString(2, answer.getContent());
            pstmt.setString(3, answer.getAuthor());  // Ensure this is set correctly
            pstmt.setBoolean(4, answer.isSolution());
            pstmt.executeUpdate();
        }
    }

    // Read all answers for a question
    public List<Answer> readAnswers(int questionId) throws SQLException {
        List<Answer> answers = new ArrayList<>();
        String query = "SELECT * FROM Answers WHERE questionId = ?";
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(query)) {
            pstmt.setInt(1, questionId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                answers.add(new Answer(
                    rs.getInt("id"),
                    rs.getInt("questionId"),
                    rs.getString("content"),
                    rs.getString("author"),
                    rs.getBoolean("isSolution")
                ));
            }
        }
        return answers;
    }

    // Update an answer
    public void updateAnswer(Answer answer) throws SQLException {
        String query = "UPDATE Answers SET content = ? WHERE id = ? AND isSolution = FALSE";
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(query)) {
            pstmt.setString(1, answer.getContent());
            pstmt.setInt(2, answer.getId());
            pstmt.executeUpdate();
        }
    }

    // Delete an answer
    public void deleteAnswer(int answerId) throws SQLException {
        String query = "DELETE FROM Answers WHERE id = ? AND isSolution = FALSE";
        try (PreparedStatement pstmt = databaseHelper.getConnection().prepareStatement(query)) {
            pstmt.setInt(1, answerId);
            pstmt.executeUpdate();
        }
    }

   
}