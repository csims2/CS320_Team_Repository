package tutoringWebsite.persist;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


import tutoringWebsite.model.*;

public class DerbyDatabase implements IDatabase{
	//code from Hake Derby Database
	static {
		try {
			Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
		} catch (Exception e) {
			throw new IllegalStateException("Could not load Derby driver");
		}
	}
	
	private interface Transaction<ResultType> {
		public ResultType execute(Connection conn) throws SQLException;
	}

	private static final int MAX_ATTEMPTS = 10;
	
	//our code for website
	@Override
	public List<Login> useLogin(final String email, final String password){
		return executeTransaction(new Transaction<List<Login>>() {
			@Override
			public List<Login> execute(Connection conn) throws SQLException {
				PreparedStatement stmt = null;
				ResultSet resultSet = null;
				
				try {
					stmt = conn.prepareStatement(
							"select Users.email, Users.password"+
							" from Users"+
							"where Users.email = ? and Users.password = ?"
							);
					
					stmt.setString(1, email);
					stmt.setString(2, password);
					
					resultSet = stmt.executeQuery();
					
				}finally {
					DBUtil.closeQuietly(resultSet);
					DBUtil.closeQuietly(stmt);
				}
			return null;
			}
				});
	}
	@Override
	public List<Announcement> createAnnouncementStudyGroup(final String message, final String date, final String time){
		return executeTransaction(new Transaction<List<Announcement>>() {
			@Override
			public List<Announcement> execute(Connection conn) throws SQLException {
				PreparedStatement stmt = null;
				ResultSet resultSet = null;
				
				try {
					stmt = conn.prepareStatement(
						"insert into Announcements(message, date, time)"
							+"values(?, ?, ?)"
							);
					stmt.setString(1, message);
					stmt.setString(2,  date);
					stmt.setString(3, time);
				}
				finally {
					DBUtil.closeQuietly(resultSet);
					DBUtil.closeQuietly(stmt);
				}
			return null;
			}
			});
		
	}
	private void loadUser(User user, ResultSet resultSet, int index) throws SQLException {
		user.setUser_Id((resultSet.getInt(index++)));
//		book.setAuthorId(resultSet.getInt(index++));  // no longer used
		user.setEmail((resultSet.getString(index++)));
		user.setPassword((resultSet.getString(index++)));
		user.setName((resultSet.getString(index++)));
		user.setUserType((resultSet.getInt(index++)));
		
	}
	
	@Override
	public List<User> createAccount(final String email, final String password, final String name, final int userType){
		return executeTransaction(new Transaction<List<User>>() {
			@Override
			public List<User> execute(Connection conn) throws SQLException {
				PreparedStatement stmt = null;
				PreparedStatement stmt1 = null;
				ResultSet resultSet = null;
				
				System.out.println("IN DERBY DATABASE");
				System.out.println("email: "+ email + " password: "+ password);
				System.out.println("name: "+ name +" userType: "+ userType);
				try {
					stmt = conn.prepareStatement(
							"insert into Users (email, password, name, userType)" +
							"values(?,?,?,?)"
					//sql to add an account to list
							);
					stmt.setString(1, email);
					stmt.setString(2, password);
					stmt.setString(3, name);
					stmt.setInt(4, userType);
					List<User> result = new ArrayList<User>();
					
					stmt.executeUpdate();
					
					System.out.println("user created");
					
					
					stmt1 = conn.prepareStatement(
							"select user_id from Users" +
							"where email = ? and password = ? and name = ? and userType = ?"
							);
					
					stmt1.setString(1, email);
					stmt1.setString(2, password);
					stmt1.setString(3, name);
					stmt1.setInt(4, userType);
					
					resultSet = stmt1.executeQuery();
				
					System.out.println("user found");
					
					
					if (resultSet.next()) {
						
						User user = new User();
						loadUser(user,resultSet,1);
						result.add(user);
					}
					
					// check if the title was found
					else {
						System.out.println("<" + email + "> was not found in the user database");
					}
					
					System.out.println("user returned");
					return result;
			}finally {
						DBUtil.closeQuietly(resultSet);
						DBUtil.closeQuietly(stmt);
						DBUtil.closeQuietly(stmt1);
					}
		
				}
			
		});
		
		
	}
	// wrapper SQL transaction function that calls actual transaction function (which has retries)
		public<ResultType> ResultType executeTransaction(Transaction<ResultType> txn) {
			try {
				return doExecuteTransaction(txn);
			} catch (SQLException e) {
				throw new PersistenceException("Transaction failed", e);
			}
		}
		
		// SQL transaction function which retries the transaction MAX_ATTEMPTS times before failing
		public<ResultType> ResultType doExecuteTransaction(Transaction<ResultType> txn) throws SQLException {
			Connection conn = connect();
			
			try {
				int numAttempts = 0;
				boolean success = false;
				ResultType result = null;
				
				while (!success && numAttempts < MAX_ATTEMPTS) {
					try {
						result = txn.execute(conn);
						conn.commit();
						success = true;
					} catch (SQLException e) {
						if (e.getSQLState() != null && e.getSQLState().equals("41000")) {
							// Deadlock: retry (unless max retry count has been reached)
							numAttempts++;
						} else {
							// Some other kind of SQLException
							throw e;
						}
					}
				}
				
				if (!success) {
					throw new SQLException("Transaction failed (too many retries)");
				}
				
				// Success!
				return result;
			} finally {
				DBUtil.closeQuietly(conn);
			}
		}
		//EDIT THIS
		private Connection connect() throws SQLException {
			Connection conn = DriverManager.getConnection("jdbc:derby:C/test/library.db;create=true");		
			// jdbc:mysql://localhost:8081/
			//jdbc:derby:DerbyDB;create=true
			//"jdbc:derby:C:/Users/isabe/Documents/Cs320/library.db;create=true"
			//jdbc:derby:C:/CS320-2019-LibraryExample-DB/library.db;create=true
			//both broken 
			// Set autocommit() to false to allow the execution of
			// multiple queries/statements as part of the same transaction.
			conn.setAutoCommit(false);
			
			return conn;
		}
		// The main method creates the database tables and loads the initial data.
		public static void main(String[] args) throws IOException {
			System.out.println("Creating tables...");
			DerbyDatabase db = new DerbyDatabase();
			db.createTables();
			
			System.out.println("Loading initial data...");
			db.loadInitialData();
			
			System.out.println("Library DB successfully initialized!");
		}
	//  creates the Authors and Books tables
		public void createTables() {
			executeTransaction(new Transaction<Boolean>() {
				@Override
				public Boolean execute(Connection conn) throws SQLException {
					PreparedStatement stmt1 = null;
					PreparedStatement stmt2 = null;
					//PreparedStatement stmt3 = null;				
				
					try {
						stmt1 = conn.prepareStatement(
							"create table Announcements (" +
							"	announcement_id integer primary key " +
							"		generated always as identity (start with 1, increment by 1), " +									
							"	message varchar(40)," +
							"	date varchar(40)," +
							"	time varchar(40)"+
							")"
						);	
						stmt1.executeUpdate();
						
						System.out.println("Announcements table created");
						
						stmt2 = conn.prepareStatement(
								"create table Users (" +
								"	user_id integer primary key " +
								"		generated always as identity (start with 1, increment by 1), " +
								"	email varchar(70)," +
								"	password varchar(70)," +
								"   name varchar(40)," +
								"	userType integer"+
								")"
						);
						stmt2.executeUpdate();
						
						System.out.println("Users table created");					
	////////////////////EDIT STUDY GROUPS TABLE MUST BE JUNCTION\\\\\\\\\\\\\\\\\\\\\\\\\\\\
						/*stmt3 = conn.prepareStatement(
								"create table StudyGroups (" +
								"	book_id   integer constraint book_id references books, " +
								"	author_id integer constraint author_id references authors " +
								")"
						);
						stmt3.executeUpdate();
						
						System.out.println("BookAuthors table created");*/				
											
						return true;
					} finally {
						DBUtil.closeQuietly(stmt1);
						DBUtil.closeQuietly(stmt2);
					}
				}
			});
		}
		// loads data retrieved from CSV files into DB tables in batch mode
		public void loadInitialData() {
			executeTransaction(new Transaction<Boolean>() {
				@Override
				public Boolean execute(Connection conn) throws SQLException {
					List<Announcement> announcementList;
					List<User> userList;
					//List<StudyGroup> studyGroupList;
					
					try {
						announcementList	= InitialData.getAnnouncement();
						userList       		= InitialData.getUser();
						//studyGroupList 		= InitialData.getStudyGroup();					
					} catch (IOException e) {
						throw new SQLException("Couldn't read initial data", e);
					}

					PreparedStatement insertAnnouncement     = null;
					PreparedStatement insertUser       = null;
					//PreparedStatement insertStudyGroup = null;

					try {
						// must completely populate Authors table before populating BookAuthors table because of primary keys
						insertAnnouncement = conn.prepareStatement("insert into Announcements (message, date, time) values (?, ?, ?)");
						for (Announcement announcement : announcementList) {
//							insertAuthor.setInt(1, author.getAuthorId());	// auto-generated primary key, don't insert this
							insertAnnouncement.setString(1, announcement.getMessage());
							//insertAnnouncement.setDate(2, x);
							//insertAnnouncement.setTime(3, x);
							insertAnnouncement.setString(2, announcement.getDate().toString());
							insertAnnouncement.setString(3, announcement.getTime().toString());
							insertAnnouncement.addBatch();
						}
						insertAnnouncement.executeBatch();
						
						System.out.println("Annoucement table populated");
						
						// must completely populate Books table before populating BookAuthors table because of primary keys
						insertUser = conn.prepareStatement("insert into Users (email, password, name, userType) values (?, ?, ?, ?)");
						for (User user : userList) {
//							insertBook.setInt(1, book.getBookId());		// auto-generated primary key, don't insert this
//							insertBook.setInt(1, book.getAuthorId());	// this is now in the BookAuthors table
							insertUser.setString(1, user.getEmail());
							insertUser.setString(2, user.getPassword());
							insertUser.setString(3, user.getName());
							insertUser.setInt(4, user.getUserType());
							insertUser.addBatch();
						}
						insertUser.executeBatch();
						
						System.out.println("User table populated");					
						
						// must wait until all Books and all Authors are inserted into tables before creating BookAuthor table
						// since this table consists entirely of foreign keys, with constraints applied
						/*insertStudyGroup = conn.prepareStatement("");
						for (StudyGroup sg: studyGroupList) {
							
						}	
						*/
						System.out.println("BookAuthors table populated");					
						
						return true;
					} finally {
						DBUtil.closeQuietly(insertAnnouncement);
						DBUtil.closeQuietly(insertUser);
						//DBUtil.closeQuietly(insertStudyGroup);				
					}
				}
			});
		}
		@Override
		public List<Announcement> createAnnouncementStudyGroup(String message, String group) {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		public List<Announcement> createAnnouncementCourse(String message, String date, String time) {
			// TODO Auto-generated method stub
			return null;
		}
}