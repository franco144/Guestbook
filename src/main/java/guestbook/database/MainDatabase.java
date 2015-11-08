package guestbook.database;

import static guestbook.database.generated.tables.Posts.POSTS;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record5;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

public class MainDatabase {

	public static void main(String[] args) {
		try {
			// firstjOOQTest();
			//inserts();
			//deletes();
			fetches();
			
		} catch (SQLException e) {
			Logger.getLogger(MainDatabase.class).error("", e);
		}
	}
	
	private static void fetches() throws SQLException {
		// fetchMap(AUTHOR.ID)
		System.out.println("\n\nRESULTS:\n");
		for( Entry<Long, Record5<Long, String, String, Integer, String>> postEntry 
				: DatabaseQueries.getPostsComplete().entrySet() ) {
			System.out.println( "post id:"+ postEntry.getKey() +"\n"+ postEntry.getValue() );
		}

		// fetchMany() 
		System.out.println("\n\nCount():\n");
		System.out.println( DatabaseQueries.getPostsCountPerAuthor() );
		
		// fetchMany() 
		System.out.println("\n\nFetch many:\n");
		System.out.println( DatabaseQueries.getAuthorsWithAtLeastTwoPubl() );
	}

	private static void inserts() throws SQLException {
		System.out.println( "insert. Succeeded ? "+ DatabaseQueries.addPost() );
		System.out.println( "insert, generated key: "+ DatabaseQueries.addPostGetKey() );
		
	}

	private static void deletes() throws SQLException {
		System.out.println( "delete. Succeeded ? "+ DatabaseQueries.deletePost() );
		
	}
	
	private static void firstjOOQTest() {
		Connection conn = null;

		String username = "root";
		String password = "";
		String url = "jdbc:mysql://localhost:3306/guestbook";

		// prepare the connection to db
		try {
			// initialize driver for connecting to database
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection(url, username, password);
			
			// let's add a simple query constructed with jOOQ's query DSL:
			DSLContext create = DSL.using(conn, SQLDialect.MYSQL);
			Result<Record> result = create.select().from(POSTS).fetch();
			
			for( Record record : result ) {
				Long id = record.getValue(POSTS.ID);
				String title = record.getValue(POSTS.TITLE);
				String description = record.getValue(POSTS.BODY);
				System.out.println( "ID: "+ id 
								+"\n Title: "+ title
								+"\n Description: "+ description );
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException ignore) {
				}
			}
		}
	}

}
