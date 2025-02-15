package databasePart1;
import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import application.User;

import java.util.ArrayList;


/**
 * The DatabaseHelper class is responsible for managing the connection to the database,
 * performing operations such as user registration, login validation, and handling invitation codes.
 */
public class DatabaseHelper {
	

	// JDBC driver name and database URL 
	static final String JDBC_DRIVER = "org.h2.Driver";   
	static final String DB_URL = "jdbc:h2:~/FoundationDatabase";  

	//  Database credentials 
	static final String USER = "sa"; 
	static final String PASS = ""; 

	private Connection connection = null;
	private Statement statement = null; 
	//	PreparedStatement pstmt

	public void connectToDatabase() throws SQLException {
		try {
			Class.forName(JDBC_DRIVER); // Load the JDBC driver
			System.out.println("Connecting to database...");
			connection = DriverManager.getConnection(DB_URL, USER, PASS);
			statement = connection.createStatement(); 
			// You can use this command to clear the database and restart from fresh.
			//statement.execute("DROP ALL OBJECTS");

			createTables();  // Create the necessary tables if they don't exist
		} catch (ClassNotFoundException e) {
			System.err.println("JDBC Driver not found: " + e.getMessage());
		}
	}
		public Connection getConnection() {
	        return this.connection;
	    }
	

	private void createTables() throws SQLException {
	    String userTable = "CREATE TABLE IF NOT EXISTS cse360users ("
	            + "id INT AUTO_INCREMENT PRIMARY KEY, "
	            + "name VARCHAR(255), "
	            + "email VARCHAR(255), "
	            + "userName VARCHAR(255) UNIQUE, "
	            + "password VARCHAR(255), "
	            + "role VARCHAR(20), "
	      + "otp VARCHAR(255), "
	      + "otpExpiry TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
	    statement.execute(userTable);

	    
	    String invitationCodesTable = "CREATE TABLE IF NOT EXISTS InvitationCodes ("
	            + "code VARCHAR(10) PRIMARY KEY, "
	            + "isUsed BOOLEAN DEFAULT FALSE)";
	    statement.execute(invitationCodesTable);
	    
	    //***RS //Create the UserRoles table if it doesn't exist already
	    String userRolesTable = "CREATE TABLE IF NOT EXISTS UserRoles ("
	    		  + "userId INT, "                     // ***User's ID, foreign key referencing cse360users
	              + "role VARCHAR(20), "              // ***User's role
	              + "PRIMARY KEY (userId, role), "    // ***Composite primary key (userId and role)
	              + "FOREIGN KEY (userId) REFERENCES cse360users(id))"; // Foreign key constraint
	    statement.execute(userRolesTable);
	    
	    
	    //RSHW2
	    // Create Questions table
	    String questionsTable = "CREATE TABLE IF NOT EXISTS Questions ("
	            + "id INT AUTO_INCREMENT PRIMARY KEY, "
	            + "title VARCHAR(255), "
	            + "description TEXT, "
	            + "author VARCHAR(255), "
	            + "isResolved BOOLEAN DEFAULT FALSE)";
	    statement.execute(questionsTable);

	    // Create Answers table
	    String answersTable = "CREATE TABLE IF NOT EXISTS Answers ("
	            + "id INT AUTO_INCREMENT PRIMARY KEY, "
	            + "questionId INT, "
	            + "content TEXT, "
	            + "author VARCHAR(255), "
	            + "isSolution BOOLEAN DEFAULT FALSE, "
	            + "FOREIGN KEY (questionId) REFERENCES Questions(id))";
	    statement.execute(answersTable);
	}	    
	    
	
	
	
	public boolean setOneTimePassword(String userName, String otp) {
        String query = "UPDATE cse360users SET otp = ?, otpExpiry = ? WHERE userName = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            Timestamp expiryTime = new Timestamp(System.currentTimeMillis() + 10 * 60 * 1000); // 10 minutes from now
        	pstmt.setString(1, otp);
            pstmt.setTimestamp(2, expiryTime); 
            pstmt.setString(3, userName);
            int updated = pstmt.executeUpdate();
            return updated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
	
	public String getOneTimePassword(String userName) {
        String query = "SELECT otp FROM cse360users WHERE username = ? AND otpExpiry > CURRENT_TIMESTAMP";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("otp");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
	
	public boolean changeAdminPassword(String userName, String oldPassword, String newPassword) {
        String checkPass = "SELECT password FROM cse360users WHERE username = ?";
        String updatePass = "UPDATE cse360users SET password = ? WHERE username = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkPass);
             PreparedStatement updateStmt = connection.prepareStatement(updatePass)) {
            checkStmt.setString(1, userName);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getString("password").equals(oldPassword)) {
                updateStmt.setString(1, newPassword);
                updateStmt.setString(2, userName);
                return updateStmt.executeUpdate() == 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
	
	public boolean verifyOtpAndResetPassword(String username, String otp, String newPassword) {
	    // First, check the OTP
	    String otpQuery = "SELECT otp, otpExpiry FROM cse360users WHERE username = ? AND otp = ? AND otpExpiry > CURRENT_TIMESTAMP()";
	    try (PreparedStatement pstmt = connection.prepareStatement(otpQuery)) {
	        pstmt.setString(1, username);
	        pstmt.setString(2, otp);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	        	//check expiry time
	        	Timestamp expiry = rs.getTimestamp("otpExpiry");
	        	if (expiry != null && expiry.after(new Timestamp(System.currentTimeMillis()))) {
	        		// OTP is valid, now update the password
		            String updateQuery = "UPDATE cse360users SET password = ?, otp = NULL, otpExpiry = NULL WHERE username = ?";
		            try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
		                updateStmt.setString(1, newPassword); // Consider using hashing for storing passwords
		                updateStmt.setString(2, username);
		                int affectedRows = updateStmt.executeUpdate();
		                return affectedRows == 1;  // Return true if the password update was successful
		            }
	            }
	            
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false; // Return false if the OTP validation fails or password update fails
	}

	
	public ResultSet getAllUsers() throws SQLException {
        String query = "SELECT name, userName, email FROM cse360users";
        return statement.executeQuery(query);
    }
	
	
	//***RS // SQL query to insert a new user role. Uses a subquery to get the userId from the cse360users table.
	public void addUserRole(User user, String role) throws SQLException {
	    String query = "INSERT INTO UserRoles (userId, role) VALUES ((SELECT id FROM cse360users WHERE userName = ?), ?)";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, user.getUserName());  // ***Set the userName parameter in the query
	        pstmt.setString(2, role); // ***Set the role parameter in the query
	        pstmt.executeUpdate(); //***Execute the update to insert the new user role
	        
	       
	    }
	}


	// Check if the database is empty
	public boolean isDatabaseEmpty() throws SQLException {
		String query = "SELECT COUNT(*) AS count FROM cse360users";
		ResultSet resultSet = statement.executeQuery(query);
		if (resultSet.next()) {
			return resultSet.getInt("count") == 0;
		}
		return true;
	}

	// Registers a new user in the database.
	// Registers a new user in the database.
		public void register(User user) throws SQLException {
			String insertUser = "INSERT INTO cse360users (name, email, userName, password) VALUES (?, ?, ?, ?)";
			try (PreparedStatement pstmt = connection.prepareStatement(insertUser)) {
				pstmt.setString(1, user.getName());
				pstmt.setString(2, user.getEmail());
				pstmt.setString(3, user.getUserName());
				pstmt.setString(4, user.getPassword());
				pstmt.executeUpdate();
			}
			
			for(String role : user.getRoles()) {
				addUserRole(user, role);
			}
		}


	// Validates a user's login credentials.
	public boolean login(User user) throws SQLException {
		String query = "SELECT * FROM cse360users WHERE userName = ? AND password = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, user.getUserName());
			pstmt.setString(2, user.getPassword());
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next();
			}
		}
	}
	
	// Checks if a user already exists in the database based on their userName.
	public boolean doesUserExist(String userName) {
	    String query = "SELECT COUNT(*) FROM cse360users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            // If the count is greater than 0, the user exists
	            return rs.getInt(1) > 0;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false; // If an error occurs, assume user doesn't exist
	}
	
	// Retrieves the roles of a user from the database using their UserName.
	// Returns the roles in an ArrayList
	public ArrayList<String> getUserRoles(String userName) throws SQLException {
	    String query = "SELECT role FROM UserRoles WHERE userId = (SELECT id FROM cse360users WHERE userName = ?)";
	    ArrayList<String> roles = new ArrayList<>();
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();
	        while(rs.next()) {
	        	roles.add(rs.getString("role"));
	        }
	    }
	    catch (SQLException e) {
	        e.printStackTrace();
	    }
	        return roles;
	}
	
	// Retrieves the name of a user from the database using their UserName.
	public String getName(String name) {
	    String query = "SELECT name FROM cse360users WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, name);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            return rs.getString("name"); // Return the name if user exists
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return null; // If no user exists or an error occurs
	}
	
	// Retrieves the email of a user from the database using their UserName.
		public String getEmail(String email) {
		    String query = "SELECT email FROM cse360users WHERE userName = ?";
		    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
		        pstmt.setString(1, email);
		        ResultSet rs = pstmt.executeQuery();
		        
		        if (rs.next()) {
		            return rs.getString("email"); // Return the email if user exists
		        }
		    } catch (SQLException e) {
		        e.printStackTrace();
		    }
		    return null; // If no user exists or an error occurs
		}
	
	// Generates a new invitation code and inserts it into the database.
	public String generateInvitationCode() {
	    String code = UUID.randomUUID().toString().substring(0, 4); // Generate a random 4-character code
	    String query = "INSERT INTO InvitationCodes (code) VALUES (?)";

	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return code;
	}
	
	// Validates an invitation code to check if it is unused.
	public boolean validateInvitationCode(String code) {
	    String query = "SELECT * FROM InvitationCodes WHERE code = ? AND isUsed = FALSE";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            // Mark the code as used
	            markInvitationCodeAsUsed(code);
	            return true;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false;
	}
	
	
	
	// Marks the invitation code as used in the database.
	private void markInvitationCodeAsUsed(String code) {
	    String query = "UPDATE InvitationCodes SET isUsed = TRUE WHERE code = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	// Closes the database connection and statement.
	public void closeConnection() {
		try{ 
			if(statement!=null) statement.close(); 
		} catch(SQLException se2) { 
			se2.printStackTrace();
		} 
		try { 
			if(connection!=null) connection.close(); 
		} catch(SQLException se){ 
			se.printStackTrace(); 
		} 
	}

}