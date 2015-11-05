package guestbook.database;

import static guestbook.database.generated.tables.Posts.POSTS;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.apache.log4j.Logger;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import guestbook.database.generated.tables.records.PostsRecord;

public class DatabaseQueries {
	
	private static String _username = "root";
	private static String _password = "";
	private static String _url = "jdbc:mysql://localhost:3306/guestbook";
	private static Logger _logger = Logger.getLogger(DatabaseQueries.class);
	
	private static Connection getConnection() throws SQLException {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			e.printStackTrace();
			throw new SQLException("Could not get JDBC instance: ", e);
		}
		return DriverManager.getConnection(_url, _username, _password);
	}
	
	private static DSLContext getCreate(Connection connection) throws SQLException {
		return DSL.using(getConnection(), SQLDialect.MYSQL);
	}
	
	/*
	 * Queries
	 */
	
	// INSERT
	public static boolean addPost() throws SQLException {
		DSLContext create = getCreate(getConnection());
		int nrInsertedRecords = create.insertInto(POSTS, POSTS.TITLE, POSTS.BODY)
				.values("My first post!", "I once made an insert query via jOOQ.")
				.execute();
		
		return nrInsertedRecords > 0;
	}
	// INSERT return the generated id
	public static long addPostGetKey() throws SQLException {
		DSLContext create = getCreate(getConnection());
		PostsRecord newPost = create.insertInto(POSTS, POSTS.TITLE, POSTS.BODY)
				.values("My first post!", "I once made an insert query via jOOQ.")
				.returning(POSTS.ID) // get me the generated id ?
				.fetchOne();
		if(newPost == null) {
			System.out.println("jOOQ could not retrieve any returned value!");
		} else {
			return newPost.getValue(POSTS.ID);
		}
		return -1;
	}
	
	// UPDATE Note: update statements are only possible on single tables.
	public static int updatePost() {
		try(Connection conn = getConnection()) {
			DSLContext create = getCreate(conn);
			create.update(POSTS)
			.set(POSTS.TITLE, "A revamped title!")
			.set(POSTS.TIMESTAMP, Timestamp.valueOf(LocalDateTime.now()))
			.where(POSTS.ID.equal(1L)) // BIGINT is converted as Long
			.execute();
			
			// how do you if this query has not been executed ?
			
		} catch (SQLException e) {
			System.out.println("jOOQ could not retrieve any returned value!");
			e.printStackTrace();
		}
		return -1;
	}
	
	// DELETE
	public static boolean deletePost() {
		try(Connection connection = getConnection()) {
			
			DSLContext create = getCreate(connection);
			String sql = create.deleteFrom(POSTS)
				.where("posts.id > 1").getSQL();
			_logger.info("Query to be executed: "+ sql);
			System.out.println("Query to be executed: "+ sql);
			
			create.query(sql).execute();
			return true;
		} catch (SQLException e) {
			_logger.error("Failed executing delete statement!", e);
		}
		return false;
	}
	
	// Fetching:
		// Map
		// List
		// ...
}
