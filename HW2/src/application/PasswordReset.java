package application;

import java.util.UUID;

import databasePart1.DatabaseHelper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;

import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage; 


//Adapted from the UserNameRecognizer class
public class PasswordReset{
	
	private final DatabaseHelper databaseHelper;
	
	public PasswordReset(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }
	
	public void showPasswordResetPage(Stage primaryStage) {
        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        TextField userNameField = new TextField();
        userNameField.setPromptText("Enter your username");

        Button requestOtpButton = new Button("Request OTP");
        Label feedbackLabel = new Label();

        requestOtpButton.setOnAction(e -> {
            String userName = userNameField.getText();
            String otp = UUID.randomUUID().toString().substring(0, 8); // Generate a simple OTP
            if (databaseHelper.setOneTimePassword(userName, otp)) {
                feedbackLabel.setText("OTP sent: " + otp); // For debugging, show OTP. In production, send via email or SMS.
            } else {
                feedbackLabel.setText("Failed to send OTP");
            }
        });

        TextField otpField = new TextField();
        otpField.setPromptText("Enter OTP");

        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("New Password");

        PasswordField confirmNewPasswordField = new PasswordField();
        confirmNewPasswordField.setPromptText("Confirm New Password");

        Button resetPasswordButton = new Button("Reset Password");
        resetPasswordButton.setOnAction(e -> {
            String otp = otpField.getText();
            String newPassword = newPasswordField.getText();
            String confirmNewPassword = confirmNewPasswordField.getText();
            if (!newPassword.equals(confirmNewPassword)) {
                feedbackLabel.setText("Passwords do not match!");
                return;
            }
            if (databaseHelper.verifyOtpAndResetPassword(userNameField.getText(), otp, newPassword)) {
                feedbackLabel.setText("Password reset successfully!");
            } else {
                feedbackLabel.setText("Invalid OTP or OTP expired.");
            }
        });

        layout.getChildren().addAll(new Label("Reset Password"), userNameField, requestOtpButton, otpField, newPasswordField, confirmNewPasswordField, resetPasswordButton, feedbackLabel);

        Scene scene = new Scene(layout, 300, 250);
        primaryStage.setTitle("Reset Password");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}