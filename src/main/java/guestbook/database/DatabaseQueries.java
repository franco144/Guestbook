package guestbook.database;

import static guestbook.database.generated.tables.Author.AUTHOR;
import static guestbook.database.generated.tables.Posts.POSTS;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record2;
import org.jooq.Record5;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.SelectConditionStep;
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
	public static Map<Long, Record5<Long, String, String, Integer, String>> getPostsComplete() {
		try(Connection conn = getConnection()) {
			
			DSLContext create = getCreate(conn);
			/*
			 * get author details and all his/her publications for authors with more than x publications.
			 */
			SelectConditionStep<Record5<Long,String,String,Integer,String>> query = create
					.select(POSTS.ID, AUTHOR.NAME, AUTHOR.SURNAME, AUTHOR.PUBLICATIONS, POSTS.TITLE)
					.from(AUTHOR)
					.join(POSTS).on(AUTHOR.ID.equal(POSTS.FK_AUTHOR_ID))
					.where(AUTHOR.PUBLICATIONS.greaterThan(6));
//			System.out.println( query.toString() );
			
			return query.fetchMap(POSTS.ID);
		} catch (SQLException e) {
			_logger.error("Failed fetching map.", e);
		}
		return null;
	}

	public static Map<Integer, Integer> getPostsCountPerAuthor() {
		try(Connection conn = getConnection()) {
			
			DSLContext create = getCreate(conn);
			/*
			 * get author details and all his/her publications for authors with more than x publications.
			 */
			Result<Record2<Integer,Integer>> result = create
					.select(AUTHOR.ID, org.jooq.impl.DSL.count())
					.from(POSTS)
					.join(AUTHOR).on(AUTHOR.ID.equal(POSTS.FK_AUTHOR_ID))
					.groupBy(AUTHOR.ID)
					.fetch();
			
			Map<Integer, Integer> toReturn = new HashMap<Integer, Integer>();
			for(Record2<Integer, Integer> record : result) {
				toReturn.put( record.value1(), record.value2() );
			}
			
			return toReturn;
		} catch (SQLException e) {
			_logger.error("Failed fetching map.", e);
		}
		return null;
	}

	public static List<Result<Record>> getAuthorsWithAtLeastTwoPubl() {
		try(Connection conn = getConnection()) {
			DSLContext create = getCreate(conn);
			
			// SELECT * FROM `cameras` WHERE user_id = ? AND deleted = 'Y'
			List<Result<Record>> result = create
					.select(AUTHOR.ID, AUTHOR.NAME)
					.from(AUTHOR)
					.where(AUTHOR.PUBLICATIONS.greaterThan(2))
					.fetchMany();
			
			return result;
		} catch (SQLException e) {
			_logger.error("Failed fetching map.", e);
		}
		return null;
	}
}

// TODO move class in a separate file
class Post {
	public Post(String title) {
		_title = title;
	}
	String _title;
}